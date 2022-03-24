package $package$.effects

import cats.ApplicativeThrow
import cats.effect.Sync

import java.util.UUID

trait GenUUID[F[_]] {
  def make: F[UUID]
  def read(str: String): F[UUID]
}

object GenUUID {
  def apply[F[_]](implicit ev: GenUUID[F]): GenUUID[F] = ev

  implicit def syncGenUUID[F[_]: Sync]: GenUUID[F] =
    new GenUUID[F] {
      def make: F[UUID] = Sync[F].delay(UUID.randomUUID())

      def read(str: String): F[UUID] =
        ApplicativeThrow[F].catchNonFatal(UUID.fromString(str))
    }
}
