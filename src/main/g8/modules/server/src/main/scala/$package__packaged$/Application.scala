package $package$

import cats.effect.std.Supervisor
import cats.effect.{IO, IOApp}
import dev.profunktor.redis4cats.log4cats._
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import $package$.config.ConfigLoader
import $package$.modules.HttpApi
import $package$.resources.{AppResources, MkHttpServer}
import $package$.security.Security

object Application extends IOApp.Simple {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] =
    ConfigLoader.load[IO].flatMap { cfg =>
      Logger[IO].info(s"Loaded config \$cfg") >>
        Supervisor[IO].use { implicit sp =>
          AppResources[IO](cfg)
            .evalMap { res =>
              Security[IO](cfg, res.postgres, res.redis).map { security =>
                cfg.serverConfig -> HttpApi[IO](security, cfg.logConfig).httpApp
              }
            }
            .flatMap { case (cfg, httpApp) =>
              MkHttpServer[IO].newEmber(cfg, httpApp)
            }
            .useForever
        }
    }

}
