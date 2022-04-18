package $package$.services

import cats._
import cats.syntax.all._
import dev.profunktor.auth.jwt.JwtToken
import eu.timepit.refined.auto.autoUnwrap
import $package$.domain._
import $package$.domain.User._
import $package$.domain.custom.exception.{EmailInUse, InvalidPassword, UserNotFound}
import $package$.domain.custom.refinements.EmailAddress
import $package$.implicits.GenericTypeOps
import $package$.security.{Crypto, Tokens}
import $package$.services.redis.RedisClient
import $package$.types.TokenExpiration

trait Auth[F[_]] {
  def newUser(userParam: CreateUser): F[JwtToken]
  def login(credentials: Credentials): F[JwtToken]
  def logout(token: JwtToken, email: EmailAddress): F[Unit]
}

object Auth {
  def apply[F[_]: MonadThrow](
    tokenExpiration: TokenExpiration,
    tokens: Tokens[F],
    users: Users[F],
    redis: RedisClient[F],
    crypto: Crypto
  ): Auth[F] =
    new Auth[F] {

      private val TokenExpiration = tokenExpiration.value

      override def newUser(userParam: CreateUser): F[JwtToken] =
        users.find(userParam.email).flatMap {
          case Some(_) =>
            EmailInUse(userParam.email).raiseError[F, JwtToken]
          case None =>
            for {
              user <- users.create(userParam, crypto.encrypt(userParam.password))
              t    <- tokens.create
              _    <- redis.put(t.value, user, TokenExpiration)
              _    <- redis.put(user.email, t.value, TokenExpiration)
            } yield t
        }

      def login(credentials: Credentials): F[JwtToken] =
        users.find(credentials.email).flatMap {
          case None =>
            UserNotFound(credentials.email).raiseError[F, JwtToken]
          case Some(user) if user.password =!= crypto.encrypt(credentials.password) =>
            InvalidPassword(credentials.email).raiseError[F, JwtToken]
          case Some(user) =>
            redis.get(credentials.email).flatMap {
              case Some(t) => JwtToken(t).pure[F]
              case None =>
                tokens.create.flatTap { t =>
                  redis.put(t.value, user.toJson, TokenExpiration) *>
                    redis.put(credentials.email, t.value, TokenExpiration)
                }
            }
        }

      def logout(token: JwtToken, email: EmailAddress): F[Unit] =
        redis.del(token.show, email)

    }
}
