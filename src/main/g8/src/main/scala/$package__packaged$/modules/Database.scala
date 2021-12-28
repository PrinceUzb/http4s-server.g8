package $package$.modules

import cats.effect._
import cats.effect.kernel.Async
import cats.effect.std.Console
import $package$.config.DBConfig
import $package$.db.algebras.{LiveUserAlgebra, UserAlgebra}
import eu.timepit.refined.auto.autoUnwrap
import natchez.Trace.Implicits.noop
import skunk._
import skunk.util.Typer

trait Database[F[_]] {
  val user: F[UserAlgebra[F]]
}

object LiveDatabase {
  def apply[F[_]: Sync: Async: Console](config: DBConfig): F[Database[F]] =
    Sync[F].delay(
      new LiveDatabase[F](config)
    )
}

final class LiveDatabase[F[_]: Async: Console] private (
  config: DBConfig
) extends Database[F] {

  private[this] val session: SessionPool[F] =
    Session.pooled[F](
      host = config.host,
      port = config.port,
      database = config.database,
      user = config.user,
      password = Some(config.password),
      max = config.poolSize,
      strategy = Typer.Strategy.SearchPath
    )

  override val user: F[UserAlgebra[F]] = session.use(LiveUserAlgebra[F])
}
