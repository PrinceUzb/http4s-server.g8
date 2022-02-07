package $package$.modules

import cats.effect._
import cats.effect.std.Console
import $package$.config.DBConfig
import $package$.db.algebras.UserAlgebra
import eu.timepit.refined.auto.autoUnwrap
import natchez.Trace.Implicits.noop
import skunk._
import skunk.util.Typer

trait Database[F[_]] {
  val user: F[UserAlgebra[F]]
}

object Database {
  def apply[F[_]: Sync: Async: Console](config: DBConfig): F[Database[F]] =
    Session
      .pooled[F](
        host = config.host,
        port = config.port,
        database = config.database,
        user = config.user,
        password = Some(config.password),
        max = config.poolSize,
        strategy = Typer.Strategy.SearchPath
      )
      .use { implicit session =>
        Sync[F].delay(new LiveDatabase[F])
      }

  final class LiveDatabase[F[_]: Async: Console](implicit
   session: Resource[F, Session[F]]
  ) extends Database[F] {

    override val user: F[UserAlgebra[F]] = UserAlgebra[F]
  }
}