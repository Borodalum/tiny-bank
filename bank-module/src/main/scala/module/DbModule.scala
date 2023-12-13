package module

import cats.effect.kernel.{Async, Sync}
import cats.syntax.functor._
import config.DbConfig
import doobie.Transactor
import pureconfig.ConfigSource

final case class DbModule[F[_]](
  transactor: Transactor[F]
)

object DbModule {
  def make[I[_] : Sync, F[_] : Async](config: ConfigSource, whichConf: String): I[DbModule[F]] =
    Sync[I].delay(config.at(whichConf).loadOrThrow[DbConfig])
      .map(conf =>
        Transactor.fromDriverManager[F](
          driver = conf.driver,
          url = conf.url,
          user = conf.user,
          pass = conf.password
        )
      )
      .map(DbModule[F](_))
}
