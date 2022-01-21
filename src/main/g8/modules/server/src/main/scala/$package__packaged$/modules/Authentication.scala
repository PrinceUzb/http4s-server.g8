package $package$.modules

import cats.effect.{Async, Sync}
import cats.implicits._
import $package$.db.algebras.{IdentityProvider, UserAlgebra}
import $package$.domain.User
import $package$.security.{AuthService, LiveAuthService}
import $package$.services.LiveIdentityService
import $package$.services.redis.RedisClient

object Authentication {
  private[this] def makeAuthService[F[_]: Async: Sync, U](
    identityProvider: IdentityProvider[F, U]
  )(implicit redisClient: RedisClient[F]): F[AuthService[F, U]] =
    LiveIdentityService[F, U](identityProvider).flatMap(LiveAuthService[F, U])

  def apply[F[_]: Async](
    userProvider: UserAlgebra[F]
  )(implicit F: Sync[F], redisClient: RedisClient[F]): F[Authentication[F]] =
    for {
      userAuth <- makeAuthService[F, User](userProvider)
    } yield new Authentication[F](userAuth)
}

final class Authentication[F[_]] private (
  val user: AuthService[F, User]
)
