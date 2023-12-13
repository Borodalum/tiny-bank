package domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.{Codec, CodecFormat, Schema}

@derive(encoder, decoder)
final case class SessionToken(
  token: String
) {
  override def toString: String = token.substring(0, 5).repeat(2)
}

object SessionToken {
  implicit val schema: Schema[SessionToken] =
    Schema.schemaForString.map[SessionToken](str => Some(SessionToken(str)))(_.token)

  implicit val codec: Codec[String, SessionToken, CodecFormat.TextPlain] =
    Codec.string.map[SessionToken](apply _)(_.token)
}