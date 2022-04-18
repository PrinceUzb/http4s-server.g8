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
import $package$.domain.User.UserWithPassword
import $package$.domain.custom.refinements.{EmailAddress, Password}
import $package$.security.Crypto
import $package$.services.Users
import $package$.stub_services.{AuthMock, UsersStub}
import $package$.utils.Generators.{booleanGen, userCredentialGen, userGen}
import $package$.utils.HttpSuite

object LoginRoutesSuite extends HttpSuite {
  def users(user: User, pass: Password, crypto: Crypto): Users[F] = new UsersStub[F] {
    override def find(
      email: EmailAddress
    ): F[Option[UserWithPassword]] =
      if (user.email.equalsIgnoreCase(email))
        UserWithPassword(user, crypto.encrypt(pass)).some.pure[IO]
      else
        none[UserWithPassword].pure[F]
  }

  test("POST login") {
    val gen = for {
      u <- userGen
      c <- userCredentialGen
      b <- booleanGen
    } yield (u, c, b)

    forall(gen) { case (user, c, isCorrect) =>
      for {
        crypto <- Crypto[IO](jwtConfig.passwordSalt.value)
        auth   <- AuthMock[IO](users(user, c.password, crypto), crypto)
        (postData, shouldReturn) =
          if (isCorrect)
            (c.copy(email = user.email), Status.Ok)
          else
            (c, Status.Forbidden)
        req    = POST(postData, uri"/auth/login")
        routes = LoginRoutes[IO](auth).routes
        res <- expectHttpStatus(routes, req)(shouldReturn)
      } yield res
    }
  }
}
