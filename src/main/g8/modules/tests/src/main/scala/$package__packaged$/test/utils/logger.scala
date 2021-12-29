package $package$.test.utils

import cats.effect._
import org.typelevel.log4cats.Logger

object logger {

  implicit object NoOp extends NoLogger

  def acc(ref: Ref[IO, List[String]]): Logger[IO] =
    new NoLogger {
      override def error(message: => String): IO[Unit] =
        ref.update(currentMessages => message :: currentMessages)
    }

  private[logger] class NoLogger extends Logger[IO] {
    def warn(message: => String): IO[Unit] = IO.unit

    def warn(t: Throwable)(message: => String): IO[Unit] = IO.unit

    def debug(t: Throwable)(message: => String): IO[Unit] = IO.unit

    def debug(message: => String): IO[Unit] = IO.unit

    def error(t: Throwable)(message: => String): IO[Unit] = IO.unit

    def error(message: => String): IO[Unit] = IO.unit

    def info(t: Throwable)(message: => String): IO[Unit] = IO.unit

    def info(message: => String): IO[Unit] = IO.unit

    def trace(t: Throwable)(message: => String): IO[Unit] = IO.unit

    def trace(message: => String): IO[Unit] = IO.unit
  }
}