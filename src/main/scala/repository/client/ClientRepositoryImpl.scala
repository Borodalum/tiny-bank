package repository.client

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._
import domain.{Client, ClientId, ClientName, ClientPassword, CreateClient}
import doobie.Transactor
import doobie.implicits._
import error.{DbError, UnexpectedDbError}
import tofu.syntax.feither.EitherFOps

private final class ClientRepositoryImpl[F[_] : MonadCancelThrow](
  sql: ClientSqls,
  transactor: Transactor[F]
) extends ClientRepository[F] {
  override def create(client: CreateClient): F[Either[DbError, Client]] =
    sql
      .create(client)
      .transact(transactor)
      .attempt
      .map {
        case Left(th) => UnexpectedDbError(th.getMessage).asLeft
        case Right(Left(err)) => err.asLeft
        case Right(Right(client)) => client.asRight
      }

  override def getById(id: ClientId): F[Either[UnexpectedDbError, Option[Client]]] =
    sql
      .getById(id)
      .transact(transactor)
      .attempt
      .map(_.leftMap(err => UnexpectedDbError.apply(err.getMessage)))

  override def getAll: F[Either[UnexpectedDbError, List[Client]]] =
    sql
      .getAll
      .transact(transactor)
      .attempt
      .map(_.leftMap(err => UnexpectedDbError.apply(err.getMessage)))

  override def removeById(id: ClientId): F[Either[DbError, Unit]] =
    sql
      .removeById(id)
      .transact(transactor)
      .attempt
      .map {
        case Left(th) => UnexpectedDbError(th.getMessage).asLeft
        case Right(Left(err)) => err.asLeft
        case _ => ().asRight
      }

  override def getIdByName(name: ClientName, pass: ClientPassword): F[Either[DbError, ClientId]] =
    sql
      .getIdByName(name, pass)
      .transact(transactor)
      .attempt
      .leftMapIn(err => UnexpectedDbError(err.getMessage))
}
