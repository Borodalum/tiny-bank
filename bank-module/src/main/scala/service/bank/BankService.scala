package service.bank

import cats.Monad
import domain.{BankAccountBalance, BankAccountId, BankAccountTransfer, ClientId, CreateBankAccount, CreateClient, Money}
import error.ApiError
import repository.account.BankAccountRepository
import repository.client.ClientRepository

trait BankService[F[_]] {
  def register(client: CreateClient): F[Either[ApiError, Unit]]
  def createAcc(acc: CreateBankAccount): F[Either[ApiError, BankAccountId]]
  def getBalance(id: BankAccountId): F[Either[ApiError, BankAccountBalance]]
  def removeAcc(id: BankAccountId): F[Either[ApiError, Unit]]
  def depositToAcc(id: BankAccountId, amount: Money): F[Either[ApiError, Unit]]
  def withdrawFromAcc(id: BankAccountId, amount: Money): F[Either[ApiError, Unit]]
  def transfer(fromId: BankAccountId, accTransfer: BankAccountTransfer): F[Either[ApiError, Unit]]
}

object BankService {
  def make[F[_] : Monad](bankRepository: BankAccountRepository[F], clientRepository: ClientRepository[F]): BankService[F] =
    new BankServiceImpl[F](bankRepository, clientRepository)
}