package $package$.security

import $package$.domain.custom.refinements.EmailAddress
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Codec, Decoder, Encoder}
import org.http4s._
import tsec.authentication._
import tsec.cipher.symmetric.jca._
import tsec.common.SecureRandomId

object AuthHelper {
  implicit val encSecureRandomId: Encoder[SecureRandomId] = Encoder.encodeString.contramap(identity)
  implicit val decSecureRandomId: Decoder[SecureRandomId] = Decoder.decodeString.map(SecureRandomId.apply)
  implicit def encBearerToken[T: Encoder]: Encoder[TSecBearerToken[T]] = deriveEncoder[TSecBearerToken[T]]
  implicit def decBearerToken[T: Decoder]: Decoder[TSecBearerToken[T]] = deriveDecoder[TSecBearerToken[T]]
  implicit def bearerTokenCodec[T: Encoder: Decoder]: Codec[TSecBearerToken[T]] =
    Codec.from(decBearerToken[T], encBearerToken[T])

  type TokenSecReqHandler[F[_], U] = SecuredRequestHandler[F, EmailAddress, U, TSecBearerToken[EmailAddress]]
  type SecReqHandler[F[_], U] = SecuredRequestHandler[F, EmailAddress, U, AuthEncryptedCookie[AES128GCM, EmailAddress]]

  type TokenSecHttpRoutes[F[_], U] =
    PartialFunction[SecuredRequest[F, U, TSecBearerToken[EmailAddress]], F[Response[F]]]

  type SecHttpRoutes[F[_], U] =
    PartialFunction[SecuredRequest[F, U, AuthEncryptedCookie[AES128GCM, EmailAddress]], F[Response[F]]]

}
