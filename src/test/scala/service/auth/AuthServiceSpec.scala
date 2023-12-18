package service.auth

import cats.effect.IO
import domain.{ClientName, ClientPassword, CreateClient, SessionToken}
import doobie.implicits._
import module.DbModule
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import pureconfig.ConfigSource
import repository.client.{ClientRepository, ClientSqls}
import repository.token.{TokenRepository, TokenSqls}

class AuthServiceSpec extends AnyFlatSpec with Matchers {
  type App[A] = IO[A]
  type Init[A] = IO[A]

  val clientSqls = ClientSqls.make
  val tokenSqls = TokenSqls.make
  val conf = ConfigSource.default
  val auth = for {
    db <- DbModule.make[Init, App](conf, "sso-db")
    clientRep = ClientRepository.make(clientSqls, db)
    tokenRep = TokenRepository.make(tokenSqls, db)
    service = AuthService.make(tokenRep, clientRep)
  } yield service
  val transactor = for {
    db <- DbModule.make[Init, App](conf, "sso-db")
  } yield db.transactor

  "AuthService test" should "return session token then login" in {
    val client = CreateClient(ClientName("user"), ClientPassword("password"))
    val token = auth
      .flatMap {service =>
        service
          .login(client)
      }

    token
      .map {
        case Left(_) => fail()
        case Right(_) => ()
      }
  }
  it should "authorize client by token" in {
    val oper = transactor
      .flatMap(trans =>
        sql"insert into tokens (userid, token) values (1, 'bydass-3123dsad-dasdas-das')".update.run.transact(trans)
      )

    val user = auth
      .flatMap{service =>
        service
          .authorize(SessionToken("bydass-3123dsad-dasdas-das"))
      }

    user
      .map {
        case Left(_) => fail()
        case Right(_) => ()
      }
  }
}
