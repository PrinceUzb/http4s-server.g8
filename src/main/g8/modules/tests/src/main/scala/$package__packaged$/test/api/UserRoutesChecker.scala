package $package$.test.api

import cats.data._
import cats.effect._
import cats.implicits._
import $package$.domain._
import $package$.routes.UserRoutes
import $package$.implicits.OptionIdOps
import $package$.domain.custom.refinements.EmailAddress
import $package$.routes._
import $package$.security.{AuthService, LiveAuthService}
import $package$.services.{IdentityService, UserService}
import org.http4s._
import org.http4s.implicits._
import org.typelevel.log4cats.Logger
import $package$.test.utils.FakeData
import tsec.authentication.credentials.SCryptPasswordStore
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.SCrypt

import java.util.UUID

trait UserRoutesChecker[F[_]: Async: Logger](implicit F: Sync[F]) {

  class FakeIdentityService(isCorrect: Boolean) extends IdentityService[F, User] {

    override def get(id: EmailAddress): OptionT[F, User] =
      OptionT(F.delay(FakeData.user().toOptWhen(isCorrect)))

    override def credentialStore: SCryptPasswordStore[F, EmailAddress] =
      new SCryptPasswordStore[F, EmailAddress] {
        override def retrievePass(id: EmailAddress): OptionT[F, PasswordHash[SCrypt]] =
          OptionT(SCrypt.hashpw[F]("Secret1!").map(_.toOptWhen(isCorrect)))
      }


    override def retrievePass(id: EmailAddress): OptionT[F, PasswordHash[SCrypt]] =
      OptionT(SCrypt.hashpw[F]("Secret1!").map(_.toOptWhen(isCorrect)))
  }

  class FakeUserService(isCorrect: Boolean) extends UserService[F] {
    override def create(user: UserData): F[Unit] =
      if (isCorrect)
        F.unit
      else
        F.raiseError(new Exception("Error"))
  }

  private def reqUserRoutes(
    request: Request[F],
    isCorrect: Boolean = true
  )(implicit authService: AuthService[F, User]): F[Response[F]] = UserRoutes[F](
    new FakeUserService(isCorrect)
  ).routes.orNotFound(request)

  def reqToAuth(method: Method, body: Option[EmailAndPassword], isCorrect: Boolean): F[Response[F]] = {

    val request = Request[F](method, uri"/user/login").withEntity(body)
    for {
      authService <- LiveAuthService[F, User](new FakeIdentityService(isCorrect))
      response <- reqUserRoutes(request)(authService)
    } yield response
  }

  private def reqToAuth(isCorrect: Boolean): F[(AuthService[F, User], Response[F])] =
    for {
      authService <- LiveAuthService[F, User](new FakeIdentityService(isCorrect))

      credentials = EmailAndPassword(FakeData.randomEmail, FakeData.Pass)
      loginReq = Request[F](Method.POST, uri"/user/login").withEntity(credentials)
      authRes <- reqUserRoutes(loginReq)(authService)
    } yield (authService, authRes)

  def reqToGetUser(method: Method, isAuthed: Boolean): F[Response[F]] =
    for {
      res <- reqToAuth(isAuthed)
      (authService, authRes) = res

      request = Request[F](method, uri"/user", headers = authRes.headers)
      response <- reqUserRoutes(request)(authService)
    } yield response
}