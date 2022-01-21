package $package$.test.service
import cats.data.OptionT
import cats.effect.{Async, Sync}
import $package$.config.RedisConfig
import $package$.domain.custom.refinements.UriAddress
import $package$.services.redis.RedisClient
import io.circe.Codec
import org.typelevel.log4cats.Logger
import tsec.authentication.BackingStore

import scala.collection.mutable

object RedisClientMock {
  def apply[F[_]: Async: Logger]: RedisClientMock[F] =
    new RedisClientMock(RedisConfig(UriAddress.unsafeFrom("redis://localhost")))
}

class RedisClientMock[F[_]: Async](redisConfig: RedisConfig)(implicit F: Sync[F], logger: Logger[F])
    extends RedisClient[F](redisConfig) {

  override def dummyBackingStore[I, V: Codec](getId: V => I)(implicit F: Sync[F]): BackingStore[F, I, V] =
    new BackingStore[F, I, V] {
      private val storageMap = mutable.HashMap.empty[I, V]

      def put(elem: V): F[V] = {
        val map = storageMap.put(getId(elem), elem)
        if (map.isEmpty)
          F.pure(elem)
        else
          F.raiseError(new IllegalArgumentException)
      }

      def get(id: I): OptionT[F, V] =
        OptionT.fromOption[F](storageMap.get(id))

      def update(v: V): F[V] = {
        storageMap.update(getId(v), v)
        F.pure(v)
      }

      def delete(id: I): F[Unit] =
        storageMap.remove(id) match {
          case Some(_) => F.unit
          case None    => F.raiseError(new IllegalArgumentException)
        }
    }

}
