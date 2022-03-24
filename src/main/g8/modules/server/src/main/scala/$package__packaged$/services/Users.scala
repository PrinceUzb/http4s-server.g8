package $package$.services

import cats.effect._
import cats.syntax.all._
import skunk._
import skunk.implicits._
import $package$.domain.ID
import $package$.domain.auth._
import $package$.effects.GenUUID
import $package$.http.auth.users._
import $package$.services.sql.UserSQL._

trait Users[F[_]] {
  def find(username: UserName): F[Option[UserWithPassword]]
  def create(username: UserName, password: EncryptedPassword): F[UserId]
}

object Users {
  def apply[F[_]: GenUUID: Sync](
    session: Resource[F, Session[F]]
  ): Users[F] =
    new Users[F] with SkunkHelper[F] {
      implicit val ev: Resource[F, Session[F]] = session
      def find(username: UserName): F[Option[UserWithPassword]] =
        prepOptQuery(selectUser, username).map(_.map { case user ~ p =>
          UserWithPassword(user.id, user.name, p)
        })

      def create(username: UserName, password: EncryptedPassword): F[UserId] =
        ID.make[F, UserId]
          .flatMap { id =>
            prepCmd(insertUser, User(id, username) ~ password).as(id)
          }
          .recoverWith { case SqlState.UniqueViolation(_) =>
            UserNameInUse(username).raiseError[F, UserId]
          }
    }

}
