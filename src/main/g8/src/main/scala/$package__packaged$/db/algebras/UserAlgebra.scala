package $package$.db.algebras

import cats.effect.{Resource, Sync}
import cats.implicits._
import $package$.db.sql.UserSql._
import $package$.domain.User
import $package$.domain.custom.refinements.{EmailAddress, Password}
import $package$.implicits.PasswordOps
import eu.timepit.refined.auto.autoUnwrap
import skunk.Session
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

trait UserAlgebra[F[_]] extends IdentityProvider[F, User] {
  def findByEmail(email: EmailAddress): F[Option[User]]
  def retrievePass(email: EmailAddress): F[Option[PasswordHash[SCrypt]]]
  def create(user: User, password: Password): F[User]
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
    extends UserAlgebra[F] {

  override def findByEmail(email: EmailAddress): F[Option[User]] =
    sessionPool.use { s =>
      s.prepare(selectByEmail).use { ps =>
        ps.option(email)
      }
    }

  override def retrievePass(email: EmailAddress): F[Option[PasswordHash[SCrypt]]] =
    sessionPool.use { s =>
      s.prepare(selectPass).use { ps =>
        ps.option(email).map(_.map(PasswordHash[SCrypt]))
      }
    }

  override def create(user: User, password: Password): F[User] =
    sessionPool.use(_.prepare(insert).use(_.execute(user, password.toHashUnsafe).as(user)))

}
