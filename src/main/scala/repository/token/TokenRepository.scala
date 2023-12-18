package repository.token

import cats.effect.MonadCancelThrow
import domain.{ClientId, SessionToken}
import error.DbError
import module.DbModule

trait TokenRepository[F[_]] {
  def create(id: ClientId): F[Either[DbError, SessionToken]]
  def check(token: SessionToken): F[Either[DbError, ClientId]]
}

object TokenRepository {
  def make[F[_] : MonadCancelThrow](sql: TokenSqls, dbModule: DbModule[F]): TokenRepository[F] =
    new TokenRepositoryImpl[F](sql, dbModule.transactor)
}
