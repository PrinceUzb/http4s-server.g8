package $package$.modules

import cats.effect._
import cats.syntax.all._
import dev.profunktor.auth.JwtAuthMiddleware
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.middleware._
import org.typelevel.log4cats.Logger
import $package$.config.LogConfig
import $package$.domain.User
import $package$.http.routes.{LoginRoutes, LogoutRoutes, UserRoutes}

import scala.concurrent.duration.DurationInt

object HttpApi {
  def apply[F[_]: Async: Logger](
    security: Security[F],
    logConfig: LogConfig
  )(implicit F: Sync[F]): HttpApi[F] =
    new HttpApi[F](security, logConfig)
}

final class HttpApi[F[_]: Async: Logger] private (
  security: Security[F],
  logConfig: LogConfig
) {
  private[this] val baseURL: String = "/"

  private[this] val usersMiddleware =
    JwtAuthMiddleware[F, User](security.userJwtAuth.value, security.usersAuth.findUser)

  // Auth routes
  private[this] val loginRoutes  = LoginRoutes[F](security.auth).routes
  private[this] val userRoutes   = UserRoutes[F](security.auth).routes
  private[this] val logoutRoutes = LogoutRoutes[F](security.auth).routes(usersMiddleware)

  // Open routes
  private[this] val openRoutes: HttpRoutes[F] =
    userRoutes <+> loginRoutes <+> logoutRoutes

  private[this] val routes: HttpRoutes[F] = Router(
    baseURL -> openRoutes
  )

  private[this] val middleware: HttpRoutes[F] => HttpRoutes[F] = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS.policy.withAllowOriginAll
        .withAllowCredentials(false)
        .apply(http)
    } andThen { http: HttpRoutes[F] =>
      Timeout(60.seconds)(http)
    }
  }

  private[this] val loggers: HttpApp[F] => HttpApp[F] = {
    { http: HttpApp[F] =>
      RequestLogger.httpApp(logConfig.httpHeader, logConfig.httpBody)(http)
    } andThen { http: HttpApp[F] =>
      ResponseLogger.httpApp(logConfig.httpHeader, logConfig.httpBody)(http)
    }
  }

  val httpApp: HttpApp[F] = loggers(middleware(routes).orNotFound)

}
