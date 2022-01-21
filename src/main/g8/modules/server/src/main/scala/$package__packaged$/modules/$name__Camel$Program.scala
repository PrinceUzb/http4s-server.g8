package $package$.modules

import cats.effect._
import cats.implicits._
import $package$.db.algebras.Algebras
import $package$.services.redis.RedisClient
import $package$.services.{LiveUserService, UserService}
import org.typelevel.log4cats.Logger

object $name;format="Camel"$Program {
  def apply[F[_]: Sync: Async: Logger](
    database: Database[F],
    redisClient: RedisClient[F]
  ): F[$name;format="Camel"$Program[F]] = {
    implicit val redis: RedisClient[F] = redisClient

    def algebrasF: F[Algebras[F]] = (
      database.user
    ).map(Algebras.apply)

    for {
      algebras <- algebrasF
      auth <- Authentication[F](algebras.user)
      userService <- LiveUserService[F](algebras.user)
    } yield new $name;format="Camel"$Program[F](auth, userService)
  }
}

final class $name;format="Camel"$Program[F[_]: Sync] private (
  val auth: Authentication[F],
  val userService: UserService[F]
)
