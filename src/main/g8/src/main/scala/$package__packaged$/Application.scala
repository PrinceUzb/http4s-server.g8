package $package$

import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Application extends IOApp {
  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = $name;format="Camel"$Server.run[IO]
}