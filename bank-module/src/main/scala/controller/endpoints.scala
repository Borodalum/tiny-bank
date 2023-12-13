package controller

import domain.{BankAccountBalance, BankAccountId, BankAccountTransfer, CreateClient, Money, SessionToken}
import error.{ApiError, BankAccLimitApiError, BankAccNotFoundApiError, InternalServerApiError, NotEnoughMoneyApiError, UnauthorizedApiError}
import sttp.model.StatusCode
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.generic.auto._
import sttp.tapir._
import sttp.tapir.{Endpoint, PublicEndpoint, endpoint, oneOf, oneOfVariant, path}

object endpoints {
  private val sessionCookie = "SessionId"
  private val appErrorEndpoints: PublicEndpoint[Unit, ApiError, Unit, Any] =
    endpoint
      .errorOut(oneOf[ApiError](
        oneOfVariant(StatusCode.InternalServerError, jsonBody[InternalServerApiError]),
        oneOfVariant(StatusCode.BadRequest, jsonBody[NotEnoughMoneyApiError]),
        oneOfVariant(StatusCode.NotFound, jsonBody[BankAccNotFoundApiError]),
        oneOfVariant(StatusCode.Unauthorized, jsonBody[UnauthorizedApiError]),
        oneOfVariant(StatusCode.BadRequest, jsonBody[BankAccLimitApiError])
      ))

  val createClientEndpoint: PublicEndpoint[CreateClient, ApiError, Unit, Any] =
    appErrorEndpoints
      .post
      .in("clients" / "register")
      .in(jsonBody[CreateClient])

  val loginClientEndpoint: Endpoint[CreateClient, Unit, ApiError, CookieValueWithMeta, Any] =
    appErrorEndpoints
      .post
      .in("clients" / "login")
      .securityIn(jsonBody[CreateClient])
      .out(setCookie(sessionCookie))

  val createBankAccountEndpoint: Endpoint[SessionToken, Unit, ApiError, BankAccountId, Any] =
    appErrorEndpoints
      .post
      .securityIn(cookie[SessionToken](sessionCookie))
      .in("accounts" / "create")
      .out(jsonBody[BankAccountId])

  val getBalanceBankAccountEndpoint: Endpoint[SessionToken, BankAccountId, ApiError, BankAccountBalance, Any] =
    appErrorEndpoints
      .get
      .securityIn(cookie[SessionToken](sessionCookie))
      .in("accounts" / "balance" / path[BankAccountId])
      .out(jsonBody[BankAccountBalance])

  val depositOnBankAccountEndpoint: Endpoint[SessionToken, (BankAccountId, Money), ApiError, Unit, Any] =
    appErrorEndpoints
      .post
      .securityIn(cookie[SessionToken](sessionCookie))
      .in("accounts" / "deposit" / path[BankAccountId])
      .in(jsonBody[Money])

  val withdrawOnBankAccountEndpoint: Endpoint[SessionToken, (BankAccountId, Money), ApiError, Unit, Any] =
    appErrorEndpoints
      .post
      .securityIn(cookie[SessionToken](sessionCookie))
      .in("accounts" / "withdraw" / path[BankAccountId])
      .in(jsonBody[Money])

  val transferFromBankAccountEndpoint: Endpoint[SessionToken, (BankAccountId, BankAccountTransfer), ApiError, Unit, Any] =
    appErrorEndpoints
      .post
      .securityIn(cookie[SessionToken](sessionCookie))
      .in("accounts" / "transfer" / path[BankAccountId])
      .in(jsonBody[BankAccountTransfer])
}