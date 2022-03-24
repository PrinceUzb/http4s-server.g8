package $package$.stub_services

import cats.Applicative
import cats.implicits.{catsSyntaxApplicativeId, toFunctorOps}
import $package$.services.redis.RedisClient

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

object RedisClientMock {
  val Redis = mutable.HashMap.empty[String, String]

  def apply[F[_]: Applicative]: RedisClient[F] = new RedisClient[F] {
    override def put(
      key: String,
      value: String,
      expire: FiniteDuration
    ): F[Unit] = Redis.put(key, value).pure[F].void

    override def get(key: String): F[Option[String]] = Redis.get(key).pure[F]

    override def del(key: String*): F[Unit] = key.foreach(Redis.remove).pure[F]
  }
}
