package $package$.http.routes

import cats.MonadThrow
import cats.syntax.all._
import org.http4s._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.JsonDecoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import $package$.domain.tokenEncoder
import $package$.services.Auth
import io.circe.refined._
import $package$.domain.User.CreateUser
import $package$.domain.custom.exception.EmailInUse

final case class UserRoutes[F[_]: JsonDecoder: MonadThrow](
  auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private[this] val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] { case req @ POST -> Root / "user" =>
    req.decodeR[CreateUser] { user =>
      auth
        .newUser(user)
        .flatMap(Created(_))
        .recoverWith { case EmailInUse(u) =>
          Conflict(u)
        }
    }

  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
