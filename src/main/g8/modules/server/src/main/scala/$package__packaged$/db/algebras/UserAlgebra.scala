package $package$.db.algebras

import cats.effect.{Resource, Sync}
import cats.implicits._
import $package$.db.sql.UserSql._
import $package$.domain.custom.refinements.EmailAddress
import $package$.domain.{User, UserData}
import $package$.utils.GenUUID
import eu.timepit.refined.auto.autoUnwrap
import skunk.Session
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

trait UserAlgebra[F[_]] extends IdentityProvider[F, User] {
  def findByEmail(email: EmailAddress): F[Option[User]]
  def retrievePass(email: EmailAddress): F[Option[PasswordHash[SCrypt]]]
  def create(user: UserData): F[User]
}

object LiveUserAlgebra {
  def apply[F[_]](
    sessionPool: Resource[F, Session[F]]
  )(implicit F: Sync[F]): F[UserAlgebra[F]] =
    F.delay(
      new LiveUserAlgebra[F](sessionPool)
    )
}

final class LiveUserAlgebra[F[_]] private (
  sessionPool: Resource[F, Session[F]]
)(implicit F: Sync[F])
    extends SkunkHelper[F]
    with UserAlgebra[F] {

  private implicit val sp: Resource[F, Session[F]] = sessionPool

  override def findByEmail(email: EmailAddress): F[Option[User]] = prepOptQuery[String, User](selectByEmail, email)

  override def retrievePass(email: EmailAddress): F[Option[PasswordHash[SCrypt]]] =
    prepOptQuery[String, String](selectPass, email).map(_.map(PasswordHash[SCrypt]))

  override def create(userData: UserData): F[User] =
    GenUUID[F].make.flatMap { uuid =>
      sessionPool.use(_.prepare(insert).use(_.unique((uuid, userData))))
    }

}
