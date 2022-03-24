package $package$.services

import cats._
import cats.syntax.all._
import dev.profunktor.auth.jwt.JwtToken
import $package$.domain._
import $package$.domain.auth._
import $package$.http.auth.users._
import $package$.implicits.GenericTypeOps
import $package$.security.{Crypto, Tokens}
import $package$.services.redis.RedisClient
import $package$.types.TokenExpiration

trait Auth[F[_]] {
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}

object Auth {
  def make[F[_]: MonadThrow](
    tokenExpiration: TokenExpiration,
    tokens: Tokens[F],
    users: Users[F],
    redis: RedisClient[F],
    crypto: Crypto
  ): Auth[F] =
    new Auth[F] {

      private val TokenExpiration = tokenExpiration.value

      def newUser(username: UserName, password: Password): F[JwtToken] =
        users.find(username).flatMap {
          case Some(_) =>
            UserNameInUse(username).raiseError[F, JwtToken]
          case None =>
            for {
              i <- users.create(username, crypto.encrypt(password))
              t <- tokens.create
              _ <- redis.put(t.value, User(i, username).toJson, TokenExpiration)
              _ <- redis.put(username.show, t.value, TokenExpiration)
            } yield t
        }

      def login(username: UserName, password: Password): F[JwtToken] =
        users.find(username).flatMap {
          case None =>
            UserNotFound(username).raiseError[F, JwtToken]
          case Some(user) if user.password =!= crypto.encrypt(password) =>
            InvalidPassword(user.name).raiseError[F, JwtToken]
          case Some(user) =>
            redis.get(username.show).flatMap {
              case Some(t) => JwtToken(t).pure[F]
              case None =>
                tokens.create.flatTap { t =>
                  redis.put(t.value, user.toJson, TokenExpiration) *>
                    redis.put(username.show, t.value, TokenExpiration)
                }
            }
        }

      def logout(token: JwtToken, username: UserName): F[Unit] =
        redis.del(token.show, username.show)

    }
}
