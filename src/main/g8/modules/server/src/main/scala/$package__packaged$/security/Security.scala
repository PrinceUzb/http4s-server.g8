package $package$.security

import cats.ApplicativeThrow
import cats.effect._
import cats.syntax.all._
import dev.profunktor.auth.jwt._
import eu.timepit.refined.auto._
import io.circe.parser.{decode => jsonDecode}
import pdi.jwt._
import skunk.Session
import $package$.config.AppConfig
import $package$.domain.auth.{ClaimContent, UserId, UserName}
import $package$.http.auth.users._
import $package$.services.redis.RedisClient
import $package$.services.{Auth, Users, UsersAuth}

object Security {
  def apply[F[_]: Sync](
    cfg: AppConfig,
    session: Resource[F, Session[F]],
    redis: RedisClient[F]
  ): F[Security[F]] = {

    val adminJwtAuth: AdminJwtAuth =
      AdminJwtAuth(
        JwtAuth
          .hmac(
            cfg.adminJwtConfig.secretKey.value.secret,
            JwtAlgorithm.HS256
          )
      )

    val userJwtAuth: UserJwtAuth =
      UserJwtAuth(
        JwtAuth
          .hmac(
            cfg.jwtConfig.tokenConfig.value.secret,
            JwtAlgorithm.HS256
          )
      )

    val adminToken = JwtToken(cfg.adminJwtConfig.adminToken.value.secret)

    for {
      adminClaim <- jwtDecode[F](adminToken, adminJwtAuth.value)
      content    <- ApplicativeThrow[F].fromEither(jsonDecode[ClaimContent](adminClaim.content))
      adminUser = AdminUser(User(UserId(content.uuid), UserName("admin")))
      tokens <- JwtExpire.make[F].map(Tokens.make[F](_, cfg.jwtConfig.tokenConfig.value, cfg.jwtConfig.tokenExpiration))
      crypto <- Crypto.make[F](cfg.jwtConfig.passwordSalt.value)
      users     = Users[F](session)
      auth      = Auth.make[F](cfg.jwtConfig.tokenExpiration, tokens, users, redis, crypto)
      adminAuth = UsersAuth.admin[F](adminToken, adminUser)
      usersAuth = UsersAuth.common[F](redis)
    } yield new Security[F](auth, adminAuth, usersAuth, adminJwtAuth, userJwtAuth)

  }
}

final class Security[F[_]] private (
  val auth: Auth[F],
  val adminAuth: UsersAuth[F, AdminUser],
  val usersAuth: UsersAuth[F, CommonUser],
  val adminJwtAuth: AdminJwtAuth,
  val userJwtAuth: UserJwtAuth
)
