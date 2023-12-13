package service.auth

import cats.Monad
import domain.{ClientId, CreateClient, SessionToken}
import error.ApiError
import repository.client.ClientRepository
import repository.token.TokenRepository

trait AuthService[F[_]] {
  def login(client: CreateClient): F[Either[ApiError, SessionToken]]
  def authorize(token: SessionToken): F[Either[ApiError, ClientId]]
}

object AuthService {
  def make[F[_] : Monad](tokenRepository: TokenRepository[F], clientRepository: ClientRepository[F]): AuthService[F] =
    new AuthServiceImpl[F](tokenRepository, clientRepository)
}
