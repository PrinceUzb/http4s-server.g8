package $package$.db.sql

import $package$.domain.User
import $package$.domain.custom.refinements._
import skunk._
import skunk.codec.all._
import skunk.implicits._
import tsec.passwordhashers._
import tsec.passwordhashers.jca.SCrypt

object UserSql {
  val dec: Decoder[User] = (uuid ~ varchar ~ varchar ~ timestamp ~ varchar).map {
    case id ~ fullName ~ email ~ createdAt ~ p =>
      User(
        id = id,
        fullName = FullName.unsafeFrom(fullName),
        createdAt = createdAt,
        email = EmailAddress.unsafeFrom(email)
      )
  }

  val enc: Encoder[User ~ PasswordHash[SCrypt]] = (uuid ~ varchar ~ varchar ~ timestamp ~ varchar).contramap {
    case (u, pass) =>
      u.id ~ u.fullName.value ~ u.email.value ~ u.createdAt ~ pass
  }

  val insert: Command[User ~ PasswordHash[SCrypt]] =
    sql"""INSERT INTO users VALUES (\$enc)""".command

  val selectByEmail: Query[String, User] =
    sql"""SELECT * FROM users WHERE email = \$varchar """.query(dec)

  val selectPass: Query[String, String] =
    sql"""SELECT password_hash FROM users WHERE email = \$varchar """.query(varchar)

}
