package $package$.services.redis

import cats._
import cats.implicits.toFunctorOps
import dev.profunktor.redis4cats.RedisCommands
import io.circe.Encoder
import $package$.implicits.GenericTypeOps

import scala.concurrent.duration.FiniteDuration

trait RedisClient[F[_]] {
  def put(key: String, value: String, expire: FiniteDuration): F[Unit]

  def put[A: Encoder](key: String, value: A, expire: FiniteDuration): F[Unit]

  def get(key: String): F[Option[String]]

  def del(key: String*): F[Unit]
}

object RedisClient {
  def apply[F[_]: MonadThrow](redis: RedisCommands[F, String, String]): RedisClient[F] = new RedisClient[F] {
    override def put(key: String, value: String, expire: FiniteDuration): F[Unit] = redis.setEx(key, value, expire)

    override def put[A: Encoder](key: String, value: A, expire: FiniteDuration): F[Unit] =
      redis.setEx(key, value.toJson, expire)

    override def get(key: String): F[Option[String]] = redis.get(key)

    override def del(key: String*): F[Unit] = redis.del(key: _*).void
  }
}
