package $package$.services

import cats.effect.Sync
import $package$.db.algebras.UserAlgebra
import $package$.domain.{User, UserData}
import org.typelevel.log4cats.Logger

trait UserService[F[_]] {
  def create(userData: UserData): F[User]
}

object LiveUserService {
  def apply[F[_]: Logger](
    userAlgebra: UserAlgebra[F]
  )(implicit F: Sync[F]): F[LiveUserService[F]] =
    F.delay(
      new LiveUserService[F](userAlgebra)
    )
}

final class LiveUserService[F[_]: Logger](
  userAlgebra: UserAlgebra[F]
)(implicit F: Sync[F])
    extends UserService[F] {

  override def create(userData: UserData): F[User] =
    userAlgebra.create(userData)
}
