package repository.client

import cats.effect.MonadCancelThrow
import domain.{Client, ClientId, ClientName, ClientPassword, CreateClient}
import error.{DbError, UnexpectedDbError}
import module.DbModule

trait ClientRepository[F[_]] {
  def create(client: CreateClient): F[Either[DbError, Client]]

  def getById(id: ClientId): F[Either[UnexpectedDbError, Option[Client]]]

  def getAll: F[Either[UnexpectedDbError, List[Client]]]

  def getIdByName(name: ClientName, pass: ClientPassword): F[Either[DbError, ClientId]]

  def removeById(id: ClientId): F[Either[DbError, Unit]]
}

object ClientRepository {
  def make[F[_] : MonadCancelThrow](sql: ClientSqls, dbModule: DbModule[F]): ClientRepository[F] =
    new ClientRepositoryImpl[F](sql, dbModule.transactor)
}


