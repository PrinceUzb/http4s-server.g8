package $package$

import cats.effect._
import cats.effect.std.Console
import cats.implicits._
import $package$.config.{ConfigLoader, HttpServerConfig}
import $package$.modules._
import eu.timepit.refined.auto.autoUnwrap
import org.http4s._
import org.http4s.blaze.server.BlazeServerBuilder
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext.global

object $name;format="Camel"$Server {

  def run[F[_]: Async: Console: Logger]: F[ExitCode] =
    for {
      conf     <- ConfigLoader.app[F]
      db       <- LiveDatabase[F](conf.dbConfig)
      programs <- $name;format="Camel"$Program[F](db)
      httpAPI <- HttpApi[F](programs, conf.logConfig)
      _     <- server[F](conf.serverConfig, httpAPI.httpApp)
    } yield ExitCode.Success

  private[this] def server[F[_]: Async](
    conf: HttpServerConfig,
    httpApp: HttpApp[F]
  ): F[Unit] =
    BlazeServerBuilder[F]
      .withExecutionContext(global)
      .bindHttp(conf.port, conf.host)
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
}