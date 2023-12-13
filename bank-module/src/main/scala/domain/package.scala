import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.Read
import io.estatico.newtype.macros.newtype
import sttp.tapir.{Codec, CodecFormat, Schema}

package object domain {
  @derive(decoder, encoder)
  @newtype
  case class ClientId(value: Long)
  object ClientId {
    implicit val reader: Read[ClientId] = Read[Long].map(ClientId.apply)
    implicit val schema: Schema[ClientId] =
      Schema.schemaForLong.map[ClientId](value => Some(ClientId(value)))(_.value)
  }

  @derive(decoder, encoder)
  @newtype
  case class ClientName(username: String)
  object ClientName {
    implicit val reader: Read[ClientName] = Read[String].map(ClientName.apply)
    implicit val schema: Schema[ClientName] =
      Schema.schemaForString.map[ClientName](str => Some(ClientName(str)))(_.username)
  }

  @derive(decoder, encoder)
  @newtype
  case class ClientPassword(password: String)
  object ClientPassword {
    implicit val reader: Read[ClientPassword] = Read[String].map(ClientPassword.apply)
    implicit val schema: Schema[ClientPassword] =
      Schema.schemaForString.map[ClientPassword](str => Some(ClientPassword(str)))(_.password)
  }

  @derive(decoder, encoder)
  @newtype
  case class BankAccountId(value: String)
  object BankAccountId {
    implicit val reader: Read[BankAccountId] = Read[String].map(BankAccountId.apply)
    implicit val schema: Schema[BankAccountId] =
      Schema.schemaForString.map[BankAccountId](str => Some(BankAccountId(str)))(_.value)
    implicit val codec: Codec[String, BankAccountId, CodecFormat.TextPlain] =
      Codec.string.map[BankAccountId](apply _)(_.value)
  }

  @derive(decoder, encoder)
  @newtype
  case class BankAccountBalance(value: BigDecimal)
  object BankAccountBalance {
    implicit val reader: Read[BankAccountBalance] = Read[BigDecimal].map(BankAccountBalance.apply)
    implicit val schema: Schema[BankAccountBalance] =
      Schema.schemaForBigDecimal.map[BankAccountBalance](str => Some(BankAccountBalance(str)))(_.value)
  }

  @derive(decoder, encoder)
  @newtype
  case class Money(value: BigDecimal)
  object Money {
    implicit val reader: Read[Money] = Read[BigDecimal].map(Money.apply)
    implicit val schema: Schema[Money] =
      Schema.schemaForBigDecimal.map[Money](str => Some(Money(str)))(_.value)
  }
}
