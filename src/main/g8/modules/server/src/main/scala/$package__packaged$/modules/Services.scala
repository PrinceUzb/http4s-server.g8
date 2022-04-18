package $package$.modules

import cats.effect.{Resource, Sync}
import skunk.Session
import $package$.effects.GenUUID
import $package$.services.Users

object Services {
  def apply[F[_]: Sync: GenUUID](implicit session: Resource[F, Session[F]]) =
    new Services[F](
      users = Users[F]
    )
}

final class Services[F[_]] private (
  val users: Users[F]
)
