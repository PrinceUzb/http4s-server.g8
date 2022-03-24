package $package$.http.routes

import cats.MonadThrow
import cats.syntax.all._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import $package$.domain.auth.{CreateUser, UserNameInUse}
import $package$.domain.tokenEncoder
import $package$.services.Auth

final case class UserRoutes[F[_]: JsonDecoder: MonadThrow](
  auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private[this] val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "user" =>
    req.decodeR[CreateUser] { user =>
      auth
        .newUser(user.username.toDomain, user.password.toDomain)
        .flatMap(Created(_))
        .recoverWith { case UserNameInUse(u) =>
          Conflict(u.show)
        }
    }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
