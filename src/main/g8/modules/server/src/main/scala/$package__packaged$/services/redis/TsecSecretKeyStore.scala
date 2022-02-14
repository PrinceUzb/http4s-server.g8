package $package$.services.redis

import cats.data.OptionT
import cats.effect.kernel.Resource
import cats.effect.{Async, Sync}
import cats.implicits.{toFlatMapOps, toFunctorOps}
import dev.profunktor.redis4cats.RedisCommands
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import tsec.cipher.symmetric.jca.{AES128GCM, SecretKey}
import $package$.implicits.{CirceDecoderOps, GenericTypeOps}

import java.time.Instant
import java.util.Base64
import scala.concurrent.duration.DurationInt

trait TsecSecretKeyStore[F[_]] {
  def getSecretKey: F[SecretKey[AES128GCM]]
}

object TsecSecretKeyStore {
  private val TSEC_KEY: String = "TSEC_SECRET_KEY"

  /** @param key
    *   the tsec SecretKey[A] [[SecretKey]]
    * @param expireAt
    *   the expiration time of secret key
    */
  case class TsecSecretKey(key: SecretKey[AES128GCM], expireAt: Instant) {
    def isExpired(now: Instant): Boolean = now.isBefore(expireAt)
  }
  object TsecSecretKey {

    implicit val encSecret: Encoder[SecretKey[AES128GCM]] = Encoder.encodeString.contramap { key =>
      Base64.getEncoder.encodeToString(key.getEncoded)
    }
    implicit val decSecret: Decoder[SecretKey[AES128GCM]] = Decoder.decodeString.map { str =>
      AES128GCM.unsafeBuildKey(Base64.getDecoder.decode(str))
    }
    implicit val dec: Decoder[TsecSecretKey] = deriveDecoder[TsecSecretKey]
    implicit val enc: Encoder[TsecSecretKey] = deriveEncoder[TsecSecretKey]
  }

  def apply[F[_]: Async](implicit
    F: Sync[F],
    redis: Resource[F, RedisCommands[F, String, String]]
  ): TsecSecretKeyStore[F] =
    new TsecSecretKeyStore[F] {

      /** @param now
        *   the current time
        * @param redisCommands
        *   the commands redis
        * @return
        *   the tsec SecretKey[A] [[SecretKey]]
        */
      private[redis] def generateKey(
        now: Instant
      )(implicit redisCommands: RedisCommands[F, String, String]): F[SecretKey[AES128GCM]] =
        AES128GCM.generateKey[F].flatMap { newKey =>
          val newTecSecretKey = TsecSecretKey(newKey, now.plusSeconds(10.days.toSeconds))
          redisCommands.setEx(TSEC_KEY, newTecSecretKey.toJson, 10.days).map { _ =>
            newKey
          }
        }

      /** @param now
        *   the current time
        * @param secretKey
        *   the tsec secret key [[TsecSecretKey]]
        * @param redisCommands
        *   the commands redis
        * @return
        *   the tsec SecretKey[A] [[SecretKey]]
        */
      private[redis] def validateAndRetrieve(now: Instant, secretKey: TsecSecretKey)(implicit
        redisCommands: RedisCommands[F, String, String]
      ): F[SecretKey[AES128GCM]] =
        if (secretKey.isExpired(now))
          F.delay(secretKey.key)
        else
          generateKey(now)

      def getSecretKey: F[SecretKey[AES128GCM]] =
        redis.use { implicit redis =>
          OptionT(redis.get(TSEC_KEY))
            .map(_.as[TsecSecretKey])
            .semiflatMap { secretKey =>
              F.delay(Instant.now()).flatMap(validateAndRetrieve(_, secretKey))
            }
            .getOrElseF(F.delay(Instant.now()).flatMap(generateKey))
        }
    }
}
