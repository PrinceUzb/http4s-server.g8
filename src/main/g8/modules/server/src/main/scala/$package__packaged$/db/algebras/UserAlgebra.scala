package $package$.db.algebras

import cats.effect.{Resource, Sync}
import cats.implicits._
import $package$.db.sql.UserSql._
import $package$.domain.custom.refinements.EmailAddress
import $package$.domain.{User, UserData}
import $package$.utils.GenUUID
import eu.timepit.refined.auto.autoUnwrap
import skunk.Session
import skunk.implicits.toIdOps
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

trait UserAlgebra[F[_]] extends IdentityProvider[F, User] {
  def findByEmail(email: EmailAddress): F[Option[User]]
  def retrievePass(email: EmailAddress): F[Option[PasswordHash[SCrypt]]]
  def create(user: UserData): F[User]
}

object UserAlgebra {
  def apply[F[_]](implicit F: Sync[F], session: Resource[F, Session[F]]): F[UserAlgebra[F]] =
    F.delay(
      new LiveUserAlgebra[F]
    )

  final class LiveUserAlgebra[F[_]](implicit F: Sync[F], session: Resource[F, Session[F]])
      extends SkunkHelper[F]
      with UserAlgebra[F] {

    override def findByEmail(email: EmailAddress): F[Option[User]] = prepOptQuery(selectByEmail, email)

    override def retrievePass(email: EmailAddress): F[Option[PasswordHash[SCrypt]]] =
      prepOptQuery(selectPass, email).map(_.map(PasswordHash[SCrypt]))

    override def create(userData: UserData): F[User] =
      GenUUID[F].make.flatMap { uuid =>
        prepQueryUnique(insert, uuid ~ userData)
      }

  }
}
