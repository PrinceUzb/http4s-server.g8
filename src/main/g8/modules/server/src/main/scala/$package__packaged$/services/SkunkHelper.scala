package $package$.services

import cats.effect.{Resource, Sync}
import cats.implicits.toFunctorOps
import skunk._

trait SkunkHelper[F[_]] {
  def prepQueryM[A, B, G[_]](query: Query[A, B])(action: PreparedQuery[F, A, B] => F[G[B]])(implicit
    session: Resource[F, Session[F]], ev: Sync[F]
  ): F[G[B]] =
    session.use {
      _.prepare(query).use(action)
    }

  def prepQuery[A, B](query: Query[A, B])(action: PreparedQuery[F, A, B] => F[B])(implicit
    session: Resource[F, Session[F]], ev: Sync[F]
  ): F[B] =
    session.use {
      _.prepare(query).use(action)
    }

  def prepQueryAll[B, G[_]](query: Query[Void, B])(implicit
    session: Resource[F, Session[F]], ev: Sync[F]
  ): F[List[B]] =
    session.use {
      _.execute(query)
    }

  def prepQueryUnique[A, B](
    query: Query[A, B],
    args: A
  )(implicit session: Resource[F, Session[F]], ev: Sync[F]): F[B] =
    prepQuery(query) {
      _.unique(args)
    }

  def prepQueryList[A, B](
    query: Query[A, B],
    args: A
  )(implicit
    session: Resource[F, Session[F]], ev: Sync[F]
  ): F[List[B]] =
    prepQueryM(query) {
      _.stream(args, 1024).compile.toList
    }

  def prepStreamQueryP[A, B](
    query: Query[A, B],
    args: A
  )(implicit
    sessionRes: Resource[F, Session[F]], ev: Sync[F]
  ): fs2.Stream[F, B] =
    for {
      session <- fs2.Stream.resource(sessionRes)
      query   <- fs2.Stream.resource(session.prepare(query))
      stream  <- query.stream(args, 128)
    } yield stream

  def prepStreamQuery[B](
    query: Query[Void, B]
  )(implicit
    sessionRes: Resource[F, Session[F]], ev: Sync[F]
  ): fs2.Stream[F, B] =
    for {
      session <- fs2.Stream.resource(sessionRes)
      query   <- fs2.Stream.resource(session.prepare(query))
      stream  <- query.stream(Void, 128)
    } yield stream

  def prepOptQuery[A, B](
    query: Query[A, B],
    args: A
  )(implicit
    session: Resource[F, Session[F]], ev: Sync[F]
  ): F[Option[B]] =
    prepQueryM(query) {
      _.option(args)
    }

  def prepCmd[A, B](cmd: Command[A])(action: PreparedCommand[F, A] => F[B])(implicit
    session: Resource[F, Session[F]], ev: Sync[F]
  ): F[B] =
    session.use {
      _.prepare(cmd).use(action)
    }

  def prepCmd[A](
    cmd: Command[A],
    args: A
  )(implicit
    session: Resource[F, Session[F]], ev: Sync[F]
  ): F[Unit] =
    prepCmd(cmd) {
      _.execute(args).void
    }
}
