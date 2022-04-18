package $package$.services

import cats.data.OptionT
import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.implicits._
import $package$.domain.{ID, User}
import $package$.domain.User._
import $package$.domain.custom.exception.EmailInUse
import $package$.domain.custom.refinements.EmailAddress
import $package$.domain.types.{EncryptedPassword, UserId}
import $package$.effects.GenUUID
import $package$.services.sql.UserSQL._

trait Users[F[_]] {
  def find(email: EmailAddress): F[Option[UserWithPassword]]
  def create(userParam: CreateUser, password: EncryptedPassword): F[User]
}

object Users {
  def apply[F[_]: GenUUID: Sync](
    implicit session: Resource[F, Session[F]]
  ): Users[F] =
    new Users[F] with SkunkHelper[F] {
      def find(email: EmailAddress): F[Option[UserWithPassword]] =
        OptionT(prepOptQuery(selectUser, email)).map { case user ~ p =>
          UserWithPassword(user, p)
        }.value

      def create(userParam: CreateUser, password: EncryptedPassword): F[User] =
        ID.make[F, UserId]
          .flatMap { id =>
            prepQueryUnique(insertUser, id ~ userParam ~ password).map(_._1)
          }
          .recoverWith { case SqlState.UniqueViolation(_) =>
            EmailInUse(userParam.email).raiseError[F, User]
          }
    }

}
