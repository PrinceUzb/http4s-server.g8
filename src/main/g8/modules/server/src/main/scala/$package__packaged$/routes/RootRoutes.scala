package $package$.routes

import cats.effect.{Async, Sync}
import cats.implicits.toSemigroupKOps
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import tsec.authentication.asAuthed
import $package$.domain.User
import $package$.security.AuthService
import $package$.utils.FileLoader

object RootRoutes {
  def apply[F[_]: Async: Sync: Logger](implicit authService: AuthService[F, User]): RootRoutes[F] =
    new RootRoutes[F]
}

class RootRoutes[F[_]: Async: Logger](implicit authService: AuthService[F, User], F: Sync[F]) {
  private[this] val supportedStaticExtensions =
    List(".css", ".png", ".ico", ".js", ".jpg", ".jpeg", ".otf", ".ttf", ".woff2", ".woff")

  implicit object dsl extends Http4sDsl[F]; import dsl._

  private[this] val publicRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case request if supportedStaticExtensions.exists(request.pathInfo.toString.endsWith) =>
      FileLoader[F].assets(request.pathInfo.toString, request)
  }

  private[this] val privateRoutes: HttpRoutes[F] = authService.securedRoutes { case secReq @ GET -> Root asAuthed _ =>
    FileLoader[F].page("index.html", secReq.request)
  }

  val routes: HttpRoutes[F] = publicRoutes <+> privateRoutes

}
