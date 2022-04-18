package $package$.http.routes

import cats.effect.IO
import cats.implicits._
import eu.timepit.refined.auto.autoUnwrap
import org.http4s.Method.POST
import org.http4s.Status
import org.http4s.client.dsl.io._
import org.http4s.implicits.http4sLiteralsSyntax
import $package$.config.jwtConfig
import $package$.domain.User
import $package$.domain.User.{CreateUser, UserWithPassword}
import $package$.domain.custom.refinements.{EmailAddress, Password}
import $package$.domain.types.EncryptedPassword
import $package$.security.Crypto
import $package$.services.Users
import $package$.stub_services.{AuthMock, UsersStub}
import $package$.utils.Generators._
import $package$.utils.HttpSuite

object UserRoutesSuite extends HttpSuite {

  def users(user: User, pass: Password, crypto: Crypto): Users[IO] = new UsersStub[IO] {
    override def find(
      email: EmailAddress
    ): F[Option[UserWithPassword]] =
      if (user.email.equalsIgnoreCase(email))
        UserWithPassword(user, crypto.encrypt(pass)).some.pure[IO]
      else
        none[UserWithPassword].pure[F]

    override def create(
      userParam: CreateUser,
      password: EncryptedPassword
    ): F[User] = user.pure[F]
  }

  test("POST create") {
    val gen = for {
      u <- userGen
      c <- createUserGen
      b <- booleanGen
    } yield (u, c, b)

    forall(gen) { case (user, newUser, conflict) =>
      for {
        crypto <- Crypto[IO](jwtConfig.passwordSalt.value)
        auth   <- AuthMock[IO](users(user, newUser.password, crypto), crypto)
        (postData, shouldReturn) =
          if (conflict)
            (newUser.copy(email = user.email), Status.Conflict)
          else
            (newUser, Status.Created)
        req    = POST(postData, uri"/auth/user")
        routes = UserRoutes[IO](auth).routes
        res <- expectHttpStatus(routes, req)(shouldReturn)
      } yield res
    }
  }
}
