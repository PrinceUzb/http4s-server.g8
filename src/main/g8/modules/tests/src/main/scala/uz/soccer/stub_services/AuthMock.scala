package $package$.stub_services

import cats.effect.Sync
import cats.implicits._
import $package$.config.jwtConfig
import $package$.security.{Crypto, JwtExpire, Tokens}
import $package$.services.{Auth, Users}

object AuthMock {

  def apply[F[_]: Sync](users: Users[F], crypto: Crypto): F[Auth[F]] =
    for {
      tokens <- JwtExpire[F].map(Tokens.make[F](_, jwtConfig.tokenConfig.value, jwtConfig.tokenExpiration))
      auth = Auth[F](jwtConfig.tokenExpiration, tokens, users, RedisClientMock[F], crypto)
    } yield auth
}
