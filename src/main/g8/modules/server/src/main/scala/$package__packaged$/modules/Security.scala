package $package$.modules

import cats.effect._
import cats.syntax.all._
import dev.profunktor.auth.jwt._
import eu.timepit.refined.auto._
import pdi.jwt._
import skunk.Session
import $package$.config.AppConfig
import $package$.domain.User
import $package$.domain.types.UserJwtAuth
import $package$.resources.AppResources
import $package$.security.{Crypto, JwtExpire, Tokens}
import $package$.services.{Auth, Users, UsersAuth}

object Security {
  def apply[F[_]: Sync](
    cfg: AppConfig,
    res: AppResources[F]
  ): F[Security[F]] = {
    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(JwtAuth.hmac(cfg.jwtConfig.tokenConfig.value.secret, JwtAlgorithm.HS256))
    implicit val postgres: Resource[F, Session[F]] = res.postgres
    for {
      tokens <- JwtExpire[F].map(Tokens.make[F](_, cfg.jwtConfig.tokenConfig.value, cfg.jwtConfig.tokenExpiration))
      crypto <- Crypto[F](cfg.jwtConfig.passwordSalt.value)
      auth      = Auth[F](cfg.jwtConfig.tokenExpiration, tokens, Users[F], res.redis, crypto)
      usersAuth = UsersAuth[F](res.redis)
    } yield new Security[F](auth, usersAuth, userJwtAuth)

  }
}

final class Security[F[_]] private (
  val auth: Auth[F],
  val usersAuth: UsersAuth[F, User],
  val userJwtAuth: UserJwtAuth
)
