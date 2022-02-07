package $package$.db.sql

import $package$.domain.custom.refinements._
import $package$.domain.{User, UserData}
import $package$.implicits.PasswordOps
import skunk._
import skunk.codec.all._
import skunk.implicits._

import java.time.LocalDateTime
import java.util.UUID

object UserSql {
  val emailCodec: Codec[EmailAddress] = varchar.imap(email => EmailAddress.unsafeFrom(email))(email => email.value)
  val fullnameCodec: Codec[FullName] = varchar.imap(fullname => FullName.unsafeFrom(fullname))(fullname => fullname.value)

  val dec: Decoder[User] = (uuid ~ emailCodec ~ fullnameCodec ~ timestamp ~ varchar).map {
    case id ~ email ~ fullName ~ createdAt ~ _ =>
      User(id, fullName, email, createdAt)
  }

  val enc: Encoder[UUID ~ UserData] = (uuid ~ emailCodec ~ fullnameCodec ~ timestamp ~ varchar).contramap { case id ~ u =>
    id ~ u.email ~ u.fullName ~ LocalDateTime.now() ~ u.password.toHashUnsafe
  }

  val insert: Query[UUID ~ UserData, User] =
    sql"""INSERT INTO users VALUES (\$enc) RETURNING *""".query(dec)

  val selectByEmail: Query[EmailAddress, User] =
    sql"""SELECT * FROM users WHERE email = \$emailCodec """.query(dec)

  val selectPass: Query[EmailAddress, String] =
    sql"""SELECT password_hash FROM users WHERE email = \$emailCodec """.query(varchar)

}
