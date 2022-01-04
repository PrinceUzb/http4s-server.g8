package $package$.domain

import cats.effect.Sync
import cats.implicits._
import $package$.domain.custom.refinements._
import $package$.domain.custom.utils.MapConvert
import $package$.domain.custom.utils.MapConvert.ValidationResult
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.{Decoder, Encoder}

import java.util.UUID

case class EmailAndPassword(email: EmailAddress, password: Password)

object EmailAndPassword {
  implicit val emailAndPasswordDecoder: Decoder[EmailAndPassword] = deriveDecoder[EmailAndPassword]
  implicit val emailAndPasswordEncoder: Encoder[EmailAndPassword] = deriveEncoder[EmailAndPassword]

  implicit def decodeMap[F[_] : Sync]: MapConvert[F, ValidationResult[EmailAndPassword]] =
    (values: Map[String, String]) =>
      (
        values
          .get("email")
          .map(EmailAddress.unsafeFrom(_).validNec)
          .getOrElse("Field [ email ] isn't defined".invalidNec),
        values
          .get("password")
          .map(Password.unsafeFrom(_).validNec)
          .getOrElse("Field [ password ] isn't defined".invalidNec)
        ).mapN(EmailAndPassword.apply).pure[F]
}