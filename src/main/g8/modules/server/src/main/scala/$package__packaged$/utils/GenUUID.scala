package $package$.utils

import cats.effect.Sync

import java.util.UUID

trait GenUUID[F[_]] {
  def make: F[UUID]
}

object GenUUID {
  def apply[F[_]](implicit ev: GenUUID[F]): GenUUID[F] = ev

  implicit def syncGenUUID[F[_]: Sync]: GenUUID[F] =
    new GenUUID[F] {
      def make: F[UUID] =
        Sync[F].delay(UUID.randomUUID())
    }
}
