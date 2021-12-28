package $package$.modules

import cats.effect._
import cats.implicits._
import org.typelevel.log4cats.Logger

object $name;format="Camel"$Program {
  def apply[F[_]: Sync: Async: Logger](
    database: Database[F]
  ): F[$name;format="Camel"$Program[F]] =
    for {
      auth <- Authentication[F](database)
    } yield new $name;format="Camel"$Program[F](auth)
}

final class $name;format="Camel"$Program[F[_]: Sync] private(
  val auth: Authentication[F]
)
