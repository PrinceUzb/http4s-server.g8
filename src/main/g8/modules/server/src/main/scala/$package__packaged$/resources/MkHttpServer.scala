package $package$.resources

import cats.effect.kernel.{Async, Resource}
import com.comcast.ip4s.{Host, Port}
import org.http4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.server.defaults.Banner
import org.typelevel.log4cats.Logger
import $package$.config.HttpServerConfig
import eu.timepit.refined.auto.autoUnwrap

trait MkHttpServer[F[_]] {
  def newEmber(cfg: HttpServerConfig, httpApp: HttpApp[F]): Resource[F, Server]
}

object MkHttpServer {
  def apply[F[_]: MkHttpServer]: MkHttpServer[F] = implicitly

  private def showEmberBanner[F[_]: Logger](s: Server): F[Unit] =
    Logger[F].info(s"\\n\${Banner.mkString("\n")}\\nHTTP Server started at \${s.address}")

  implicit def forAsyncLogger[F[_]: Async: Logger]: MkHttpServer[F] =
    (cfg: HttpServerConfig, httpApp: HttpApp[F]) =>
      EmberServerBuilder
        .default[F]
        .withHostOption(Host.fromString(cfg.host))
        .withPort(Port.fromString(cfg.port.toString()).getOrElse(throw new Exception("")))
        .withHttpApp(httpApp)
        .build
        .evalTap(showEmberBanner[F])
}
