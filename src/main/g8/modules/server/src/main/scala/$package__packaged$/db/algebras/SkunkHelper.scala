package $package$.db.algebras

import cats.effect.{Resource, Sync}
import cats.implicits.toFunctorOps
import skunk._

abstract class SkunkHelper[F[_]: Sync] {
  def prepQuery[A, B, G[_]](query: Query[A, B])(action: PreparedQuery[F, A, B] => F[G[B]])(implicit
    sessionPool: Resource[F, Session[F]]
  ): F[G[B]] =
    sessionPool.use { session =>
      session.prepare(query).use(action)
    }

  def prepQueryU[A, B](
    query: Query[A, B]
  )(action: PreparedQuery[F, A, B] => F[B])(implicit sessionPool: Resource[F, Session[F]]): F[B] =
    sessionPool.use { session =>
      session.prepare(query).use(action)
    }

  def prepListQuery[A, B](
    query: Query[A, B],
    args: A
  )(implicit
    sessionPool: Resource[F, Session[F]]
  ): F[List[B]] =
    prepQuery(query) {
      _.stream(args, 1024).compile.toList
    }

  def prepStreamQuery[A, B](
    query: Query[A, B],
    args: A
  )(implicit
    sessionPool: Resource[F, Session[F]]
  ): fs2.Stream[F, B] =
    for {
      session <- fs2.Stream.resource(sessionPool)
      query   <- fs2.Stream.resource(session.prepare(query))
      stream  <- query.stream(args, 128)
    } yield stream

  def prepOptQuery[A, B](
    query: Query[A, B],
    args: A
  )(implicit
    sessionPool: Resource[F, Session[F]]
  ): F[Option[B]] =
    prepQuery(query) {
      _.option(args)
    }

  def prepCmd[A, B](cmd: Command[A])(action: PreparedCommand[F, A] => F[B])(implicit
    sessionPool: Resource[F, Session[F]]
  ): F[B] =
    sessionPool.use { session =>
      session.prepare(cmd).use(action)
    }

  def prepCmd[A](
    cmd: Command[A],
    args: A
  )(implicit
    sessionPool: Resource[F, Session[F]]
  ): F[Unit] =
    prepCmd(cmd) {
      _.execute(args).void
    }
}
