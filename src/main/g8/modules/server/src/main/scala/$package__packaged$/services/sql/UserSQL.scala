package $package$.services.sql

import skunk._
import skunk.implicits._
import $package$.domain.auth.{EncryptedPassword, UserName}
import $package$.http.auth.users.User

object UserSQL {
  val codec: Codec[User ~ EncryptedPassword] =
    (userId ~ userName ~ encPassword).imap { case i ~ n ~ p =>
      User(i, n) ~ p
    } { case u ~ p =>
      u.id ~ u.name ~ p
    }

  val selectUser: Query[UserName, User ~ EncryptedPassword] =
    sql"""
        SELECT * FROM users
        WHERE name = \$userName
       """.query(codec)

  val insertUser: Command[User ~ EncryptedPassword] =
    sql"""
        INSERT INTO users
        VALUES (\$codec)
        """.command

}
