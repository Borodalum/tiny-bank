package repository.token

import domain.{ClientId, SessionToken}
import doobie.implicits._
import doobie.{ConnectionIO, Query0, Update0}

trait TokenSqls {
  def create(id: ClientId): ConnectionIO[SessionToken]
  def check(token: SessionToken): ConnectionIO[Option[ClientId]]
}

object TokenSqls {
  object sqls {
    def createSql(id: ClientId): Update0 =
      sql"""
        insert into tokens (userid)
        values (${id.value})
      """.update

    def checkSql(token: SessionToken): Query0[Long] =
      sql"""
        select userid
        from tokens
        where token=${token.token} and expires_at > CURRENT_TIMESTAMP
      """.query[Long]
  }

  private final class Impl extends TokenSqls {
    import sqls._

    override def create(id: ClientId): ConnectionIO[SessionToken] =
      createSql(id)
        .withUniqueGeneratedKeys[String]("token")
        .map(SessionToken.apply)

    override def check(token: SessionToken): ConnectionIO[Option[ClientId]] =
      checkSql(token)
        .map(ClientId.apply)
        .option
  }

  def make: TokenSqls = new Impl
}
