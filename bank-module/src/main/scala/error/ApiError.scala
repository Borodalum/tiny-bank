package error

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.derevo.schema

sealed trait ApiError

@derive(encoder, decoder, schema)
final case class InternalServerApiError(
  message: String
) extends ApiError

@derive(encoder, decoder, schema)
final case class UnauthorizedApiError(
  message: String
) extends ApiError

@derive(encoder, decoder, schema)
final case class BankAccNotFoundApiError(
  message: String
) extends ApiError

@derive(encoder, decoder, schema)
final case class BankAccLimitApiError(
  message: String
) extends ApiError

@derive(encoder, decoder, schema)
final case class NotEnoughMoneyApiError(
  message: String
) extends ApiError