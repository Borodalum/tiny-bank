package service.bank

import cats.Monad
import cats.implicits._
import cats.syntax._
import domain.{BankAccountBalance, BankAccountId, BankAccountTransfer, ClientId, CreateBankAccount, CreateClient, Money}
import error.{ApiError, BankAccLimitApiError, BankAccNotFoundApiError, DbError, InternalServerApiError, NotEnoughMoneyApiError, UnexpectedDbError}
import repository.account.BankAccountRepository
import repository.client.ClientRepository
import service.bank.BankServiceImpl.{fromDbError, raiseLimitExceed, raiseNotEnough, raiseNotFound}
import tofu.syntax.feither._

private final class BankServiceImpl[F[_] : Monad](
  bankRepository: BankAccountRepository[F],
  clientRepository: ClientRepository[F]
) extends BankService[F] {
  private val accLimit = 3

  override def register(client: CreateClient): F[Either[ApiError, Unit]] =
    clientRepository
      .create(client)
      .mapIn(_ => ())
      .leftMapIn(fromDbError)

  override def createAcc(acc: CreateBankAccount): F[Either[ApiError, BankAccountId]] =
    bankRepository
      .getByClientId(acc.clientId)
      .flatMap {
        case Left(th) => fromDbError(th).asLeft[BankAccountId].pure[F]
        case Right(l) if l.size >= accLimit =>
          raiseLimitExceed.asLeft[BankAccountId].pure[F]
        case Right(_) => bankRepository
          .create(acc)
          .leftMapIn(fromDbError)
          .mapIn(acc => acc.accId)
      }

  override def removeAcc(clientId: ClientId, id: BankAccountId): F[Either[ApiError, Unit]] =
    bankRepository
      .getById(id)
      .flatMap {
        case Left(_) | Right(None) => raiseNotFound.asLeft[Unit].pure[F]
        case Right(Some(acc)) if acc.clientId != clientId => raiseNotFound.asLeft[Unit].pure[F]
        case Right(Some(_)) => bankRepository
          .removeById(id)
          .leftMapIn(fromDbError)
      }

  override def depositToAcc(id: BankAccountId, amount: Money): F[Either[ApiError, Unit]] =
    bankRepository
      .depositById(id, amount)
      .leftMapIn(fromDbError)

  override def withdrawFromAcc(clientId: ClientId, id: BankAccountId, amount: Money): F[Either[ApiError, Unit]] = {
    bankRepository
      .getById(id)
      .flatMap {
        case Left(_) | Right(None) => raiseNotFound.asLeft[Unit].pure[F]
        case Right(Some(acc)) if acc.clientId != clientId => raiseNotFound.asLeft[Unit].pure[F]
        case Right(Some(_)) => bankRepository
          .getBalanceById(id)
          .flatMap {
            case Left(err) => fromDbError(err).asLeft[Unit].pure[F]
            case Right(Some(balance)) if balance.value < amount.value => raiseNotEnough.asLeft[Unit].pure[F]
            case Right(None) => raiseNotFound.asLeft[Unit].pure[F]
            case Right(Some(_)) => bankRepository
              .withdrawById(id, amount)
              .leftMapIn(fromDbError)
          }
      }
  }

  override def transfer(clientId: ClientId, fromId: BankAccountId, accTransfer: BankAccountTransfer): F[Either[ApiError, Unit]] =
    bankRepository
      .getById(fromId)
      .flatMap {
        case Left(_) | Right(None) => raiseNotFound.asLeft[Unit].pure[F]
        case Right(Some(acc)) if acc.clientId != clientId => raiseNotFound.asLeft[Unit].pure[F]
        case Right(Some(_)) => bankRepository
          .getBalanceById(fromId)
          .flatMap {
            case Left(err) => fromDbError(err).asLeft[Unit].pure[F]
            case Right(Some(balance)) if balance.value < accTransfer.amount.value =>
              raiseNotEnough.asLeft[Unit].pure[F]
            case Right(None) => raiseNotFound.asLeft[Unit].pure[F]
            case Right(Some(_)) => bankRepository
              .withdrawById(fromId, accTransfer.amount)
              .flatMap {
                case Left(err) => fromDbError(err).asLeft[Unit].pure[F]
                case Right(_) => bankRepository
                  .depositById(accTransfer.bankIdTo, accTransfer.amount)
                  .leftMapIn(fromDbError)
              }
          }
      }

  override def getBalance(clientId: ClientId, id: BankAccountId): F[Either[ApiError, BankAccountBalance]] = {
    bankRepository
      .getById(id)
      .flatMap {
        case Left(_) | Right(None) => raiseNotFound.asLeft[BankAccountBalance].pure[F]
        case Right(Some(acc)) if acc.clientId != clientId => raiseNotFound.asLeft[BankAccountBalance].pure[F]
        case Right(Some(_)) => bankRepository
          .getBalanceById(id)
          .flatMap {
            case Left(err) => fromDbError(err).asLeft[BankAccountBalance].pure[F]
            case Right(None) => raiseNotFound.asLeft[BankAccountBalance].pure[F]
            case Right(Some(balance)) => balance.asRight[ApiError].pure[F]
          }
      }
  }
}

object BankServiceImpl {
  private def fromDbError(error: DbError): ApiError =
    error match {
      case UnexpectedDbError(_) => InternalServerApiError("Internal server error occurred. Try later")
    }

  private def raiseLimitExceed: ApiError = BankAccLimitApiError("Accounts limit exceed")
  private def raiseNotEnough: ApiError = NotEnoughMoneyApiError("Not enough money at balance")
  private def raiseNotFound: ApiError = BankAccNotFoundApiError("Account not found")
}