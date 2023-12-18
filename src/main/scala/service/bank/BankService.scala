package service.bank

import cats.Monad
import domain.{BankAccountBalance, BankAccountId, BankAccountTransfer, ClientId, CreateBankAccount, CreateClient, Money}
import error.ApiError
import repository.account.BankAccountRepository
import repository.client.ClientRepository

trait BankService[F[_]] {
  def register(client: CreateClient): F[Either[ApiError, Unit]]
  def createAcc(acc: CreateBankAccount): F[Either[ApiError, BankAccountId]]
  def getBalance(clientId: ClientId, id: BankAccountId): F[Either[ApiError, BankAccountBalance]]
  def removeAcc(clientId: ClientId, id: BankAccountId): F[Either[ApiError, Unit]]
  def depositToAcc(id: BankAccountId, amount: Money): F[Either[ApiError, Unit]]
  def withdrawFromAcc(clientId: ClientId, id: BankAccountId, amount: Money): F[Either[ApiError, Unit]]
  def transfer(clientId: ClientId, fromId: BankAccountId, accTransfer: BankAccountTransfer): F[Either[ApiError, Unit]]
}

object BankService {
  def make[F[_] : Monad](bankRepository: BankAccountRepository[F], clientRepository: ClientRepository[F]): BankService[F] =
    new BankServiceImpl[F](bankRepository, clientRepository)
}