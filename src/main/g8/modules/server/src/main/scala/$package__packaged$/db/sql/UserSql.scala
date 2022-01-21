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
  val dec: Decoder[User] = (uuid ~ varchar ~ varchar ~ timestamp ~ varchar).map {
    case id ~ fullName ~ email ~ createdAt ~ _ =>
      User(
        id = id,
        fullName = FullName.unsafeFrom(fullName),
        createdAt = createdAt,
        email = EmailAddress.unsafeFrom(email)
      )
  }

  val enc: Encoder[UUID ~ UserData] = (uuid ~ varchar ~ varchar ~ timestamp ~ varchar).contramap { case id ~ u =>
    id ~ u.fullName.value ~ u.email.value ~ LocalDateTime.now() ~ u.password.toHashUnsafe
  }

  val insert: Query[UUID ~ UserData, User] =
    sql"""INSERT INTO users VALUES (\$enc) RETURNING *""".query(dec)

  val selectByEmail: Query[String, User] =
    sql"""SELECT * FROM users WHERE email = \$varchar """.query(dec)

  val selectPass: Query[String, String] =
    sql"""SELECT password_hash FROM users WHERE email = \$varchar """.query(varchar)

}
