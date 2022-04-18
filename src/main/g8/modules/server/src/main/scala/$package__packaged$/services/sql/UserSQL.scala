package $package$.services.sql

import skunk._
import skunk.implicits._
import $package$.domain.User.CreateUser
import $package$.domain.custom.refinements.EmailAddress
import $package$.domain.types._
import $package$.domain.{Role, User}

object UserSQL {
  val userId: Codec[UserId] = identity[UserId]

  private val Columns = userId ~ userName ~ email ~ gender ~ encPassword ~ role

  val encoder: Encoder[UserId ~ CreateUser ~ EncryptedPassword] =
    Columns.contramap { case i ~ u ~ p =>
      i ~ u.name ~ u.email ~ u.gender ~ p ~ Role.USER
    }
  val decoder: Decoder[User ~ EncryptedPassword] =
    Columns.map { case i ~ n ~ e ~ g ~ p ~ r =>
      User(i, n, e, g, r) ~ p
    }

  val selectUser: Query[EmailAddress, User ~ EncryptedPassword] =
    sql"""SELECT * FROM users WHERE email = \$email""".query(decoder)

  val insertUser: Query[UserId ~ CreateUser ~ EncryptedPassword, User ~ EncryptedPassword] =
    sql"""INSERT INTO users VALUES (\$encoder) returning *""".query(decoder)

}
