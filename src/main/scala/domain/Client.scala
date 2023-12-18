package domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.derevo.schema

@derive(decoder, encoder, schema)
case class Client(id: ClientId, name: ClientName, password: ClientPassword)

@derive(decoder, encoder, schema)
case class CreateClient(name: ClientName, password: ClientPassword)

@derive(decoder, encoder)
case class BankAccount(clientId: ClientId, accId: BankAccountId, balance: BankAccountBalance)

@derive(decoder, encoder)
case class CreateBankAccount(clientId: ClientId)

@derive(decoder, encoder)
case class BankAccountTransfer(bankIdTo: BankAccountId, amount: Money)