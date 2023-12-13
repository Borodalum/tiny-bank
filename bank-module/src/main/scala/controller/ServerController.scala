package controller

import cats.Monad
import cats.implicits._
import controller.endpoints.{createBankAccountEndpoint, createClientEndpoint, depositOnBankAccountEndpoint, getBalanceBankAccountEndpoint, loginClientEndpoint, transferFromBankAccountEndpoint, withdrawOnBankAccountEndpoint}
import domain.{BankAccountId, CreateBankAccount}
import error.{BankAccNotFoundApiError, InternalServerApiError, NotEnoughMoneyApiError}
import repository.account.BankAccountRepository
import repository.client.ClientRepository
import service.auth.AuthService
import service.bank.BankService
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.server.ServerEndpoint
import tofu.syntax.feither._

final class ServerController[F[_] : Monad](
  bankService: BankService[F],
  authService: AuthService[F],
  clientRepository: ClientRepository[F]
) {
  private val createClientServerEndpoint: ServerEndpoint[Any, F] =
    createClientEndpoint
      .serverLogic(createClient =>
        clientRepository
          .create(createClient)
          .mapIn(_ => ())
          .leftMapIn(_ => InternalServerApiError("Internal error occurred. Try later"))
    )

  private val loginClientServerEndpoint: ServerEndpoint[Any, F] =
    loginClientEndpoint
      .serverSecurityLogic(client => authService.login(client))
      .serverLogicSuccess {token => _ =>
        CookieValueWithMeta(
          token.token,
          expires = None,
          maxAge = None,
          domain = None,
          path = None,
          secure = true,
          httpOnly = true,
          sameSite = None,
          otherDirectives = Map.empty
        ).pure[F]
      }

  private val createBankAccountServerEndpoint: ServerEndpoint[Any, F] =
    createBankAccountEndpoint
      .serverSecurityLogic(authService.authorize)
      .serverLogic {userId => _ =>
        bankService
          .createAcc(CreateBankAccount(userId))
      }

  private val getBalanceBankAccServerEndpoint: ServerEndpoint[Any, F] =
    getBalanceBankAccountEndpoint
      .serverSecurityLogic(authService.authorize)
      .serverLogic {_ => bankId =>
        bankService
          .getBalance(bankId)
      }

  private val depositOnBankAccServerEndpoint: ServerEndpoint[Any, F] =
    depositOnBankAccountEndpoint
      .serverSecurityLogic(authService.authorize)
      .serverLogic {_ => params =>
        val (bankId, deposit) = params
        bankService
          .depositToAcc(bankId, deposit)
      }

  private val withdrawOnBankAccServerEndpoint: ServerEndpoint[Any, F] =
    withdrawOnBankAccountEndpoint
      .serverSecurityLogic(authService.authorize)
      .serverLogic {_ => params =>
        val (bankId, withdraw) = params
//        bankAccountRepository
//          .getBalanceById(bankId)
//          .flatMap {
//            case Left(_) => InternalServerApiError("Internal error occurred. Try later").asLeft[Unit].pure[F]
//            case Right(Some(balance)) if balance.value >= withdraw.value =>
//               bankAccountRepository
//                .withdrawById(bankId, withdraw)
//                .leftMapIn(_ => InternalServerApiError("Internal error occurred. Try later"))
//            case Right(Some(_)) => NotEnoughMoneyApiError("Not enough money in bank account").asLeft[Unit].pure[F]
//            case _ => BankAccNotFoundApiError("Bank account not found").asLeft[Unit].pure[F]
//          }
//          // костыль, но чет скала не хочет сабтайпинг видеть
//          // - мб я не слишком глубоко природу этого понимаю
//          .leftMapIn(apiErr => apiErr)
        bankService
          .withdrawFromAcc(bankId, withdraw)
      }

  private val transferFromBankAccServerEndpoint: ServerEndpoint[Any, F] =
    transferFromBankAccountEndpoint
      .serverSecurityLogic(authService.authorize)
      .serverLogic {_ => params =>
        val (bankIdFrom, transfer) = params
//        bankAccountRepository
//          .getBalanceById(bankIdFrom)
//          .flatMap {
//            case Left(_) => InternalServerApiError("Internal error occurred. Try later").asLeft[Unit].pure[F]
//            case Right(Some(balance)) if balance.value >= transfer.amount.value =>
//              bankAccountRepository
//                .withdrawById(bankIdFrom, transfer.amount)
//                .leftMapIn(_ => InternalServerApiError("Internal error occurred. Try later")) *> bankAccountRepository
//                .depositById(transfer.bankIdTo, transfer.amount)
//                .leftMapIn(_ => InternalServerApiError("Internal error occurred. Try later"))
//            case Right(Some(_)) => NotEnoughMoneyApiError("Not enough money in bank account").asLeft[Unit].pure[F]
//            case _ => BankAccNotFoundApiError("Bank account not found").asLeft[Unit].pure[F]
//          }
//          // костыль, но чет скала не хочет сабтайпинг видеть
//          // - мб я не слишком глубоко природу этого понимаю
//          .leftMapIn(apiErr => apiErr)
        bankService
          .transfer(bankIdFrom, transfer)
      }

  val apiEndpoints: List[ServerEndpoint[Any, F]] =
    List(
      createClientServerEndpoint,
      loginClientServerEndpoint,
      createBankAccountServerEndpoint,
      getBalanceBankAccServerEndpoint,
      depositOnBankAccServerEndpoint,
      withdrawOnBankAccServerEndpoint,
      transferFromBankAccServerEndpoint
    )
}
