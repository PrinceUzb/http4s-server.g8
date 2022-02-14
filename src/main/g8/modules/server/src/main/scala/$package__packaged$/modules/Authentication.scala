package $package$.modules

import cats.effect.{Async, Sync}
import cats.implicits._
import $package$.db.algebras.{IdentityProvider, UserAlgebra}
import $package$.domain.User
import $package$.security.AuthService
import $package$.services.LiveIdentityService
import $package$.services.redis.RedisClient

object Authentication {
  private[this] def makeAuthService[F[_]: Async: Sync, U](
    identityProvider: IdentityProvider[F, U]
  )(implicit redisClient: RedisClient[F]): F[AuthService[F, U]] =
    for {
      identityService <- LiveIdentityService[F, U](identityProvider)
      key <- redisClient.secretKeyStore.getSecretKey
      authService <- AuthService[F, U](identityService, key)
    } yield authService

  def apply[F[_]: Async](
    userProvider: UserAlgebra[F]
  )(implicit F: Sync[F], redisClient: RedisClient[F]): F[Authentication[F]] =
    makeAuthService[F, User](userProvider).map { userAuth =>
      new Authentication[F](userAuth)
    }
}

final class Authentication[F[_]] private (
  val user: AuthService[F, User]
)
