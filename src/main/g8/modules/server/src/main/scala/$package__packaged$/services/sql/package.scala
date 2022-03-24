package $package$.services

import skunk.Codec
import skunk.codec.all.{uuid, varchar}
import skunk.data.{Arr, Type}
import $package$.domain.auth.{EncryptedPassword, UserId, UserName}

import java.util.UUID
import scala.util.Try

package object sql {

  def parseUUID: String => Either[String, UUID] = s =>
    Try(Right(UUID.fromString(s))).getOrElse(Left(s"Invalid argument: [ \$s ]"))

  val _uuid: Codec[Arr[UUID]] = Codec.array(_.toString, parseUUID, Type._uuid)

  val listUUID: Codec[List[UUID]] = _uuid.imap(_.flattenTo(List))(l => Arr(l: _*))

  val userId: Codec[UserId]     = uuid.imap[UserId](UserId.apply)(_.value)
  val userName: Codec[UserName] = varchar.imap[UserName](UserName.apply)(_.value)

  val encPassword: Codec[EncryptedPassword] = varchar.imap[EncryptedPassword](EncryptedPassword.apply)(_.value)

}
