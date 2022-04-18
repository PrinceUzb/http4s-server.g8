package $package$.domain
import derevo.cats.{eqv, show}
import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import dev.profunktor.auth.jwt.JwtSymmetricAuth
import eu.timepit.refined.types.string.NonEmptyString
import io.estatico.newtype.macros.newtype
import $package$.types.uuid
import io.circe.refined._
import eu.timepit.refined.cats._

import java.util.UUID
import javax.crypto.Cipher

object types {
  @derive(decoder, encoder, show)
  @newtype case class UserName(value: NonEmptyString)

  @derive(decoder, encoder, eqv, show, uuid)
  @newtype case class UserId(value: UUID)

  @derive(decoder, encoder, eqv, show)
  @newtype case class EncryptedPassword(value: String)

  @newtype case class EncryptCipher(value: Cipher)

  @newtype case class DecryptCipher(value: Cipher)

  @newtype case class UserJwtAuth(value: JwtSymmetricAuth)

}
