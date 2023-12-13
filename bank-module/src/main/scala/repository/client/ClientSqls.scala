package repository.client

import cats.implicits._
import domain.{Client, ClientId, ClientName, ClientPassword, CreateClient}
import doobie.implicits._
import doobie.{ConnectionIO, Query0, Update0}
import error.UnexpectedDbError

trait ClientSqls {
  def create(client: CreateClient): ConnectionIO[Either[UnexpectedDbError, Client]]

  def getById(id: ClientId): ConnectionIO[Option[Client]]

  def getAll: ConnectionIO[List[Client]]

  def getIdByName(name: ClientName, pass: ClientPassword): ConnectionIO[ClientId]

  def removeById(id: ClientId): ConnectionIO[Either[UnexpectedDbError, Unit]]
}

object ClientSqls {

  object sqls {
    def createSql(client: CreateClient): Update0 =
      sql"""
        insert into clients (username, password)
        values (${client.name.username}, crypt(${client.password.password}, gen_salt('md5')))
      """.update

    def getByIdSql(id: ClientId): Query0[Client] =
      sql"""
        select (userid, username, password)
        from clients
        where userid=${id.value}
      """.query[Client]

    val getAllSql: Query0[Client] =
      sql"""
        select *
        from clients
      """.query[Client]

    def removeByIdSql(id: ClientId): Update0 =
      sql"""
        delete from clients
        where userid=${id.value}
      """.update

    def findByNameSql(name: ClientName): Query0[Client] =
      sql"""
        select (userid, username, password)
        from clients
        where username=${name.username}
      """.query[Client]

    def findByAllSql(name: ClientName, pass: ClientPassword): Query0[ClientId] =
      sql"""
        select (userid)
        from clients
        where username=${name.username} and password=crypt(${pass.password}, password)
      """.query[ClientId]
  }

  private final class Impl extends ClientSqls {

    import sqls._

    override def create(client: CreateClient): ConnectionIO[Either[UnexpectedDbError, Client]] =
      findByNameSql(client.name).option.flatMap {
        case Some(_) => UnexpectedDbError("").asLeft[Client].pure[ConnectionIO]
        case None =>
          createSql(client)
            .withUniqueGeneratedKeys[ClientId]("userid")
            .map((id: ClientId) =>
              Client(id, client.name, client.password).asRight
            )
      }

    override def getById(id: ClientId): ConnectionIO[Option[Client]] =
      getByIdSql(id).option

    override def getAll: ConnectionIO[List[Client]] = getAllSql.to[List]

    override def removeById(id: ClientId): ConnectionIO[Either[UnexpectedDbError, Unit]] =
      removeByIdSql(id).run.map {
        case 0 => UnexpectedDbError(id.toString).asLeft
        case _ => ().asRight
      }

    override def getIdByName(name: ClientName, pass: ClientPassword): ConnectionIO[ClientId] =
      findByAllSql(name, pass).unique
  }

  def make: ClientSqls = new Impl
}
