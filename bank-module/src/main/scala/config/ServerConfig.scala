package config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

final case class ServerConfig(
  host: String,
  port: Int
)

object ServerConfig {
  implicit val reader: ConfigReader[ServerConfig] = deriveReader
}
