package $package$.utils

import cats.effect._
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.{Request, Response, StaticFile}

trait FileLoader[F[_]] {

  def page(path: String, request: Request[F])(implicit dsl: Http4sDsl[F]): F[Response[F]]

  def assets(path: String, request: Request[F])(implicit dsl: Http4sDsl[F]): F[Response[F]]
}

object FileLoader {

  def apply[F[_]](implicit ev: FileLoader[F]): FileLoader[F] = ev

  val PagePath: String = "/views/"

  implicit def fileLoader[F[_]: Async]: FileLoader[F] =
    new FileLoader[F] {

      override def page(
        path: String,
        request: Request[F]
      )(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
        import dsl._
        StaticFile.fromResource(PagePath + path, request.some).getOrElseF(NotFound("Page not found!"))
      }

      override def assets(
        path: String,
        request: Request[F]
      )(implicit dsl: Http4sDsl[F]): F[Response[F]] = {
        import dsl._
        StaticFile.fromResource(path, request.some).getOrElseF(NotFound())
      }
    }
}
