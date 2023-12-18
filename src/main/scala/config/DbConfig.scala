package config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

final case class DbConfig(
  driver: String,
  url: String,
  user: String,
  password: String
)

object DbConfig {
  implicit val reader: ConfigReader[DbConfig] = deriveReader
}
