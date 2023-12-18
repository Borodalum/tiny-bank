package service.bank

import cats.effect.IO
import cats.implicits._
import domain.{BankAccount, BankAccountBalance, BankAccountId, ClientId, ClientName, ClientPassword, CreateBankAccount, CreateClient, Money}
import doobie.implicits._
import doobie.Transactor
import error.{DbError, NotFoundDbError, UnexpectedDbError}
import module.DbModule
import org.mockito.MockitoSugar.mock
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import pureconfig.ConfigSource
import repository.account.{BankAccountRepository, BankAccountSqls}
import repository.client.{ClientRepository, ClientSqls}

class BankServiceSpec extends AnyFlatSpec with Matchers {
  type App[A] = IO[A]
  type Init[A] = IO[A]

  val clientSqls = ClientSqls.make
  val bankSqls = BankAccountSqls.make
  val conf = ConfigSource.default
  val bank = for {
    db <- DbModule.make[Init, App](conf, "bank-db")
    clientRep = ClientRepository.make(clientSqls, db)
    bankRep = BankAccountRepository.make(bankSqls, db)
    service = BankService.make(bankRep, clientRep)
  } yield service
  val transactor = for {
    db <- DbModule.make[Init, App](conf, "bank-db")
  } yield db.transactor

  "BankService test" should "create account in bank" in {
    val clientId = ClientId(1)
    val createdId = bank
      .flatMap { service =>
        service
          .createAcc(CreateBankAccount(clientId))
      }

    createdId.map{
      case Left(_) => fail()
      case Right(_) => ()
    }
  }

  it should "return bank account balance by id" in {
    val accountId = "3212 2131 2313 1"
    val clientId = ClientId(1)
    val oper = transactor
      .flatMap(trans =>
        sql"insert into accounts values (1, $accountId, 0)".update.run.transact(trans)
      )

    val balance = bank
      .flatMap { service =>
        service
          .getBalance(clientId, BankAccountId(accountId))
      }

    balance
      .map {
        case Left(_) => fail()
        case Right(b) => b.value shouldBe 0
      }
  }

  it should "register client in bank" in {
    val createClient = CreateClient(ClientName("user"), ClientPassword("password"))
    val create = bank
      .flatMap {service =>
        service
          .register(createClient)
      }

    create
      .map {
        case Left(_) => fail()
        case Right(_) => ()
      }
  }

  it should "deposit money into acc by id" in {
    val accountId = "3212 2131 2313 1"
    val clientId = ClientId(1)
    val oper = transactor
      .flatMap(trans =>
        sql"insert into accounts (userid, accid, balance) values (1, $accountId, 0)".update.run.transact(trans)
      )

    val balance = bank
      .flatMap { service =>
        service
          .getBalance(clientId, BankAccountId(accountId))
      }

    balance
      .map {
        case Left(_) => fail()
        case Right(b) => b.value shouldBe 0
      }

    val deposit = bank
      .flatMap {service =>
        service
          .depositToAcc(BankAccountId(accountId), Money(100))
      }

    deposit
      .map {
        case Left(_) => fail()
        case Right(_) => ()
      }

    val balance1 = bank
      .flatMap { service =>
        service
          .getBalance(clientId, BankAccountId(accountId))
      }

    balance1
      .map {
        case Left(_) => fail()
        case Right(b) => b.value shouldBe 100
      }
  }

  it should "withdraw money from account less then balance" in {
    val accountId = "3212 2131 2313 1"
    val clientId = ClientId(1)
    val oper = transactor
      .flatMap(trans =>
        sql"insert into accounts values (1, $accountId, 150)".update.run.transact(trans)
      )

    val withdraw = bank
      .flatMap { service =>
        service
          .withdrawFromAcc(clientId, BankAccountId(accountId), Money(100))
      }

    withdraw
      .map {
        case Left(_) => fail()
        case Right(_) => ()
      }
  }

  it should "withdraw money from account greater then balance" in {
    val accountId = "3212 2131 2313 1"
    val clientId = ClientId(1)
    val oper = transactor
      .flatMap(trans =>
        sql"insert into accounts values (1, $accountId, 150)".update.run.transact(trans)
      )

    val withdraw = bank
      .flatMap { service =>
        service
          .withdrawFromAcc(clientId, BankAccountId(accountId), Money(300))
      }

    withdraw
      .map {
        case Left(_) => ()
        case Right(_) => fail()
      }
  }
}
