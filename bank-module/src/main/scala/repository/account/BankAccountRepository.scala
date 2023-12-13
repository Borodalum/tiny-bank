package repository.account

import cats.effect.MonadCancelThrow
import domain.{BankAccount, BankAccountBalance, BankAccountId, Money, ClientId, CreateBankAccount}
import error.{DbError, UnexpectedDbError}
import module.DbModule

trait BankAccountRepository[F[_]] {
  def create(bankAccount: CreateBankAccount): F[Either[DbError, BankAccount]]

  def getByClientId(id: ClientId): F[Either[UnexpectedDbError, List[BankAccount]]]

  def getById(id: BankAccountId): F[Either[UnexpectedDbError, Option[BankAccount]]]

  def removeById(id: BankAccountId): F[Either[DbError, Unit]]

  def getBalanceById(id: BankAccountId): F[Either[DbError, Option[BankAccountBalance]]]

  def depositById(id: BankAccountId, deposit: Money): F[Either[DbError, Unit]]

  def withdrawById(id: BankAccountId, withdraw: Money): F[Either[DbError, Unit]]
}

object BankAccountRepository {
  def make[F[_]: MonadCancelThrow](sql: BankAccountSqls, dbModule: DbModule[F]): BankAccountRepository[F] =
    new BankAccountRepositoryImpl[F](sql, dbModule.transactor)
}
