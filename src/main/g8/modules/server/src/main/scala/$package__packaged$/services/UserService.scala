package crowdlabel.services


import cats.data.NonEmptyList
import cats.data.OptionT
import cats.effect.*
import cats.implicits.*
import crowdlabel.db.algebras.*
import crowdlabel.domain.*
import crowdlabel.domain.Invite.Status
import crowdlabel.domain.Project.TaskType
import crowdlabel.domain.TaskCoefficient.*
import eu.timepit.refined.types.numeric.{NonNegDouble, PosDouble, NonNegInt}
import crowdlabel.domain.custom.refinements.Password
import crowdlabel.implicits.*
import eu.timepit.refined.types.numeric.NonNegDouble
import eu.timepit.refined.collection.Empty
import org.typelevel.log4cats.Logger
import eu.timepit.refined.auto.autoUnwrap
import java.time.LocalDateTime
import java.util.UUID
import scala.annotation.tailrec
import scala.util.Random

trait UserService[F[_]] {
  def create(userForm: UserForm): F[Unit]
}

object LiveUserService {
  def apply[F[_] : Logger](
    userAlgebra: F[UserAlgebra[F]]
  )(implicit F: Sync[F]): F[LiveUserService[F]] =
    F.delay(
      new LiveUserService[F](userAlgebra)
    )
}

final class LiveUserService[F[_]: Logger](
  userAlgebraF: F[UserAlgebra[F]]
)(implicit F: Sync[F])
    extends UserService[F] {
  private def userByUserForm(userForm: UserForm, rateId: UUID): User =
    User(
      id = UUID.randomUUID(),
      fullName = userForm.fullName,
      passwordExpiresAt = LocalDateTime.now().plusDays(60),
      createdAt = LocalDateTime.now(),
      email = userForm.email,
      phone = userForm.phone,
      rateId = rateId
    )


  override def create(userForm: UserForm): F[Unit] = {
    for {
      userAlgebra <- userAlgebraF
      user = userByUserForm(userForm, rate.id)
      _ <- userAlgebra.create(user, userForm.password)
    } yield ()
  }
}
