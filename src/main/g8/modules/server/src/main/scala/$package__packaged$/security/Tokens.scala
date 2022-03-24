package $package$.security


import cats.Monad
import cats.syntax.all._
import dev.profunktor.auth.jwt._
import eu.timepit.refined.auto._
import io.circe.syntax._
import pdi.jwt._
import $package$.effects.GenUUID
import $package$.implicits.GenericTypeOps
import $package$.types.{JwtAccessTokenKeyConfig, TokenExpiration}

trait Tokens[F[_]] {
  def create: F[JwtToken]
}

object Tokens {
  def make[F[_]: GenUUID: Monad](
      jwtExpire: JwtExpire[F],
      config: JwtAccessTokenKeyConfig,
      exp: TokenExpiration
  ): Tokens[F] =
    new Tokens[F] {
      def create: F[JwtToken] =
        for {
          uuid  <- GenUUID[F].make
          claim <- jwtExpire.expiresIn(JwtClaim(uuid.toJson), exp)
          secretKey = JwtSecretKey(config.secret)
          token <- jwtEncode[F](claim, secretKey, JwtAlgorithm.HS256)
        } yield token
    }
}