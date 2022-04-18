package $package$.services

import cats._
import cats.data.OptionT
import dev.profunktor.auth.jwt.JwtToken
import pdi.jwt.JwtClaim
import $package$.domain.User
import $package$.implicits.CirceDecoderOps
import $package$.services.redis.RedisClient

trait UsersAuth[F[_], A] {
  def findUser(token: JwtToken)(claim: JwtClaim): F[Option[A]]
}

object UsersAuth {
  def apply[F[_]: Functor](
    redis: RedisClient[F]
  ): UsersAuth[F, User] =
    new UsersAuth[F, User] {
      def findUser(token: JwtToken)(claim: JwtClaim): F[Option[User]] =
        OptionT(redis.get(token.value))
          .map(_.as[User])
          .value

    }

}
