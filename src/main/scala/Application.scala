import cats.effect.kernel.Sync
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.catsSyntaxApplicativeId
import com.comcast.ip4s.{IpLiteralSyntax, Port, Host}
import config.ServerConfig
import controller.ServerController
import module.DbModule
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import pureconfig.ConfigSource
import repository.client.{ClientRepository, ClientSqls}
import repository.token.{TokenRepository, TokenSqls}
import repository.account.{BankAccountRepository, BankAccountSqls}
import service.auth.AuthService
import service.bank.BankService
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI

object Application extends IOApp {
  private type Init[A] = IO[A]
  private type App[A] = IO[A]

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      _            <- println("Bank started").pure[App]
      conf         = ConfigSource.default
      server       <- Sync[Init].delay(conf.at("server").loadOrThrow[ServerConfig])
      dbModuleBank <- DbModule.make[Init, App](conf, "bank-db")
      dbModuleSso  <- DbModule.make[Init, App](conf, "sso-db")

      clientSqls       = ClientSqls.make
      clientRepository = ClientRepository.make[App](clientSqls, dbModuleBank)

      tokenSqls        = TokenSqls.make
      tokenRepository  = TokenRepository.make[App](tokenSqls, dbModuleSso)

      authService      = AuthService.make[App](tokenRepository, clientRepository)

      bankAccountSqls  = BankAccountSqls.make
      bankAccountRepository = BankAccountRepository.make[App](bankAccountSqls, dbModuleBank)

      bankService      = BankService.make[App](bankAccountRepository, clientRepository)

      controller       = new ServerController[App](bankService, authService, clientRepository)

      openApi = OpenAPIDocsInterpreter()
        .toOpenAPI(es = controller.apiEndpoints.map(_.endpoint), "Tiny bank API", "0.1")
        .toYaml

      routes = Http4sServerInterpreter[App]().toRoutes(controller.apiEndpoints ++ SwaggerUI[IO](openApi))
      httpApp = Router("/" -> routes).orNotFound
      service: EmberServerBuilder[App] = EmberServerBuilder
        .default[App]
        .withHost(Host.fromString(server.host).getOrElse(host"0.0.0.0"))
        .withPort(Port.fromInt(server.port).getOrElse(port"8080"))
        .withHttpApp(httpApp)
    } yield service)
      .flatMap(_.build.useForever)
      .as(ExitCode.Success)
}
