package $package$.modules

import cats.effect.{Async, Sync}
import cats.implicits._
import $package$.db.algebras.IdentityProvider
import $package$.domain.User
import $package$.security.{AuthService, LiveAuthService}
import $package$.services.LiveIdentityService

object Authentication {
  private[this] def makeAuthService[F[_]: Async: Sync, U](
    identityProvider: IdentityProvider[F, U]
  ): F[AuthService[F, U]] = LiveIdentityService[F, U](identityProvider).flatMap(LiveAuthService[F, U])

  def apply[F[_]: Async](
    database: Database[F]
  )(implicit F: Sync[F]): F[Authentication[F]] =
    for {
      userProvider <- database.user
      userAuth     <- makeAuthService[F, User](userProvider)
    } yield new Authentication[F](userAuth)
}

final class Authentication[F[_]] private (
  val user: AuthService[F, User]
)
