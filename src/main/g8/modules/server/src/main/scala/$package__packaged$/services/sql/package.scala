package $package$.services

import eu.timepit.refined.types.string.NonEmptyString
import skunk._
import skunk.codec.all._
import skunk.data.{Arr, Type}
import $package$.domain.custom.refinements.{EmailAddress, Tel}
import $package$.domain.types.{EncryptedPassword, UserName}
import $package$.domain.{Gender, Role}
import $package$.types.IsUUID
import eu.timepit.refined.auto.autoUnwrap

import java.util.UUID
import scala.util.Try

package object sql {

  def parseUUID: String => Either[String, UUID] = s =>
    Try(Right(UUID.fromString(s))).getOrElse(Left(s"Invalid argument: [ \$s ]"))

  val _uuid: Codec[Arr[UUID]] = Codec.array(_.toString, parseUUID, Type._uuid)

  val listUUID: Codec[List[UUID]] = _uuid.imap(_.flattenTo(List))(l => Arr(l: _*))

  def identity[A: IsUUID]: Codec[A] = uuid.imap[A](IsUUID[A]._UUID.get)(IsUUID[A]._UUID.apply)

  val userName: Codec[UserName] = varchar.imap[UserName](name => UserName(NonEmptyString.unsafeFrom(name)))(_.value)

  val encPassword: Codec[EncryptedPassword] = varchar.imap[EncryptedPassword](EncryptedPassword.apply)(_.value)

  val email: Codec[EmailAddress] = varchar.imap[EmailAddress](EmailAddress.unsafeFrom)(_.value)

  val tel: Codec[Tel] = varchar.imap[Tel](Tel.unsafeFrom)(_.value)

  val gender: Codec[Gender] = `enum`[Gender](_.value, Gender.find, Type("gender"))

  val role: Codec[Role] = `enum`[Role](_.value, Role.find, Type("role"))

}
