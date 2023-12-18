package repository.token

import cats.effect.MonadCancelThrow
import cats.implicits._
import domain.{ClientId, SessionToken}
import doobie.Transactor
import doobie.implicits._
import error.{DbError, NotFoundDbError, UnexpectedDbError}
import tofu.syntax.feither._

private final class TokenRepositoryImpl[F[_] : MonadCancelThrow](
  sql: TokenSqls, transactor: Transactor[F]
) extends TokenRepository[F] {

  override def create(id: ClientId): F[Either[DbError, SessionToken]] =
    sql.create(id)
      .transact(transactor)
      .attempt
      .leftMapIn(err => UnexpectedDbError(err.getMessage))

  override def check(token: SessionToken): F[Either[DbError, ClientId]] =
    sql.check(token)
      .transact(transactor)
      .attempt
      .leftMapIn(err => UnexpectedDbError(err.getMessage))
      .flatMapIn(_.toRight(NotFoundDbError("Client not found")))
}
