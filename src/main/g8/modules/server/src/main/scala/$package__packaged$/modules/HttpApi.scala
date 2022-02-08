package $package$.modules

import cats.effect._
import cats.implicits._
import $package$.config.LogConfig
import $package$.domain.User
import $package$.routes._
import $package$.security.AuthService
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.staticcontent.webjarServiceBuilder
import org.http4s.server.{Router, middleware}
import org.typelevel.log4cats.Logger

object HttpApi {
  def apply[F[_]: Async: Logger](
    program: $name;format="Camel"$Program[F],
    logConfig: LogConfig
  )(implicit F: Sync[F]): F[HttpApi[F]] =
    F.delay(
      new HttpApi[F](program, logConfig)
    )
}

final class HttpApi[F[_]: Async: Logger] private (
  program: $name;format="Camel"$Program[F],
  logConfig: LogConfig
) {
  private[this] val root: String        = "/"
  private[this] val webjarsPath: String = "/webjars"
  implicit val authUser: AuthService[F, User] = program.auth.user

  private[this] val rootRoutes: HttpRoutes[F] = RootRoutes[F].routes
  private[this] val userRoutes: HttpRoutes[F] = UserRoutes[F](program.userService).routes
  private[this] val webjars: HttpRoutes[F]    = webjarServiceBuilder[F].toRoutes

  private[this] val loggedRoutes: HttpRoutes[F] => HttpRoutes[F] = http =>
    middleware.Logger.httpRoutes(logConfig.httpHeader, logConfig.httpBody)(http)

  val httpApp: HttpApp[F] =
    loggedRoutes(
      Router(
        webjarsPath              -> webjars,
        UserRoutes.prefixPath    -> userRoutes,
        root                     -> rootRoutes,
      )
    ).orNotFound
}
