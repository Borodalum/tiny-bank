package repository.account

import cats.effect.MonadCancelThrow
import domain.{BankAccount, BankAccountBalance, BankAccountId, ClientId, CreateBankAccount, Money}
import doobie.Transactor
import doobie.implicits._
import cats.implicits._
import error.{DbError, UnexpectedDbError}

private final class BankAccountRepositoryImpl[F[_] : MonadCancelThrow](
                                                                        sql: BankAccountSqls,
                                                                        transactor: Transactor[F]
                                                                      ) extends BankAccountRepository[F] {

  override def create(bankAccount: CreateBankAccount): F[Either[DbError, BankAccount]] =
    sql
      .create(bankAccount)
      .transact(transactor)
      .attempt
      .map {
        case Left(th) => UnexpectedDbError("penis").asLeft[BankAccount]
        case Right(acc) => acc.asRight
      }

  override def getByClientId(id: ClientId): F[Either[UnexpectedDbError, List[BankAccount]]] =
    sql
      .getByClientId(id)
      .transact(transactor)
      .attempt
      .map(_.leftMap(err => UnexpectedDbError(err.getMessage)))

  override def getById(id: BankAccountId): F[Either[UnexpectedDbError, Option[BankAccount]]] =
    sql
      .getById(id)
      .transact(transactor)
      .attempt
      .map(_.leftMap(err => UnexpectedDbError(err.getMessage)))

  override def removeById(id: BankAccountId): F[Either[DbError, Unit]] =
    sql
      .removeById(id)
      .transact(transactor)
      .attempt
      .map {
        case Left(th) => UnexpectedDbError(th.getMessage).asLeft
        case Right(Left(err)) => err.asLeft
        case Right(Right(_)) => ().asRight
      }

  override def getBalanceById(id: BankAccountId): F[Either[DbError, Option[BankAccountBalance]]] =
    sql
      .getBalanceById(id)
      .transact(transactor)
      .attempt
      .map(_.leftMap(err => UnexpectedDbError(err.getMessage)))

  override def depositById(id: BankAccountId, deposit: Money): F[Either[DbError, Unit]] =
    sql
      .depositById(id, deposit)
      .transact(transactor)
      .attempt
      .map {
        case Left(th) => UnexpectedDbError(th.getMessage).asLeft
        case Right(Left(err)) => err.asLeft
        case Right(Right(_)) => ().asRight
      }


  override def withdrawById(id: BankAccountId, withdraw: Money): F[Either[DbError, Unit]] =
    sql
      .withdrawById(id, withdraw)
      .transact(transactor)
      .attempt
      .map {
        case Left(th) => UnexpectedDbError(th.getMessage).asLeft
        case Right(Left(err)) => err.asLeft
        case Right(Right(_)) => ().asRight
      }
}
