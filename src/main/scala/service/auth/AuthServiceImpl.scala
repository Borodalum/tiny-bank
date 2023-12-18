package service.auth

import cats.Monad
import domain.{ClientId, CreateClient, SessionToken}
import error.{ApiError, InternalServerApiError, UnauthorizedApiError}
import repository.client.ClientRepository
import repository.token.TokenRepository
import tofu.syntax.feither._

final class AuthServiceImpl[F[_] : Monad](
  tokenRepository: TokenRepository[F],
  clientRepository: ClientRepository[F]
) extends AuthService[F] {

  override def login(client: CreateClient): F[Either[ApiError, SessionToken]] = {
    clientRepository
      .getIdByName(client.name, client.password)
      .doubleFlatMap(tokenRepository.create)
      .leftMapIn(_ => InternalServerApiError("Internal error occurred. Try later"))
  }

  override def authorize(token: SessionToken): F[Either[ApiError, ClientId]] =
    tokenRepository
      .check(token)
      .leftMapIn(_ => UnauthorizedApiError("Not authorized"))

}
