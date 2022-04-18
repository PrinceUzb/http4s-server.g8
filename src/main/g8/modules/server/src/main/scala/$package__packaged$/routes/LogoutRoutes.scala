package $package$.http.routes

import cats.Monad
import cats.syntax.all._
import dev.profunktor.auth.AuthHeaders
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import $package$.domain.User
import $package$.services.Auth

final case class LogoutRoutes[F[_]: Monad](
  auth: Auth[F]
) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/auth"

  private[this] val httpRoutes: AuthedRoutes[User, F] = AuthedRoutes.of { case ar @ POST -> Root / "logout" as user =>
    AuthHeaders
      .getBearerToken(ar.req)
      .traverse_(auth.logout(_, user.email)) *> NoContent()
  }

  def routes(authMiddleware: AuthMiddleware[F, User]): HttpRoutes[F] = Router(
    prefixPath -> authMiddleware(httpRoutes)
  )

}
