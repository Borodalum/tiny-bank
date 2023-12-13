package repository.account

import cats.implicits._
import domain._
import doobie.implicits._
import doobie.{ConnectionIO, Query0, Update0}
import error.NotFoundDbError

trait BankAccountSqls {
  def create(bankAccount: CreateBankAccount): ConnectionIO[BankAccount]

  def getByClientId(id: ClientId): ConnectionIO[List[BankAccount]]

  def getById(id: BankAccountId): ConnectionIO[Option[BankAccount]]

  def removeById(id: BankAccountId): ConnectionIO[Either[NotFoundDbError, Unit]]

  def getBalanceById(id: BankAccountId): ConnectionIO[Option[BankAccountBalance]]

  def depositById(id: BankAccountId, deposit: Money): ConnectionIO[Either[NotFoundDbError, Unit]]

  def withdrawById(id: BankAccountId, withdraw: Money): ConnectionIO[Either[NotFoundDbError, Unit]]
}

object BankAccountSqls {

  object sqls {
    def createSql(bankAccount: CreateBankAccount): Update0 =
      sql"""
            insert into accounts (userId)
            values (${bankAccount.clientId.value})
          """.update

    def getByClientIdSql(id: ClientId): Query0[BankAccount] =
      sql"""
            select *
            from accounts
            where userId=${id.value}
          """.query[BankAccount]

    def getByIdSql(id: BankAccountId): Query0[BankAccount] =
      sql"""
            select (userId, accId, balance)
            from accounts
            where accId=${id.value}
          """.query[BankAccount]

    def removeByIdSql(id: BankAccountId): Update0 =
      sql"""
            delete from accounts
            where accId=${id.value}
          """.update

    def getBalanceByIdSql(id: BankAccountId): Query0[BankAccountBalance] =
      sql"""
           select (balance)
           from accounts
           where accid=${id.value}
         """.query[BankAccountBalance]

    def depositByIdSql(id: BankAccountId, deposit: Money): Update0 =
      sql"""
           update accounts
           set balance=balance + ${deposit.value}
           where accid=${id.value}
         """.update

    def withdrawByIdSql(id: BankAccountId, withdraw: Money): Update0 =
      sql"""
           update accounts
           set balance=balance - ${withdraw.value}
           where accid=${id.value}
         """.update
  }

  private final class Impl extends BankAccountSqls {

    import sqls._

    override def create(bankAccount: CreateBankAccount): ConnectionIO[BankAccount] = {
      createSql(bankAccount)
        .withUniqueGeneratedKeys[BankAccountId]("accid")
        .map((accId: BankAccountId) =>
          BankAccount(bankAccount.clientId, accId, BankAccountBalance(0))
        )
    }

    override def getByClientId(id: ClientId): ConnectionIO[List[BankAccount]] =
      getByClientIdSql(id).to[List]

    override def getById(id: BankAccountId): ConnectionIO[Option[BankAccount]] =
      getByIdSql(id).option

    override def removeById(id: BankAccountId): ConnectionIO[Either[NotFoundDbError, Unit]] =
      removeByIdSql(id).run.map {
        case 0 => NotFoundDbError(id.value).asLeft
        case _ => ().asRight
      }

    override def getBalanceById(id: BankAccountId): ConnectionIO[Option[BankAccountBalance]] =
      getBalanceByIdSql(id).option

    override def depositById(id: BankAccountId, deposit: Money): ConnectionIO[Either[NotFoundDbError, Unit]] =
      depositByIdSql(id, deposit).run.map {
        case 0 => NotFoundDbError(id.value).asLeft
        case _ => ().asRight
      }

    override def withdrawById(id: BankAccountId, withdraw: Money): ConnectionIO[Either[NotFoundDbError, Unit]] =
      withdrawByIdSql(id, withdraw).run.map {
        case 0 => NotFoundDbError(id.value).asLeft
        case _ => ().asRight
      }
  }

  def make: BankAccountSqls = new Impl
}

// post login - по авторизации возвращает токен
// get introspect - говорит активен ли токен
// get userinfo - возвращает информацию о пользователе
