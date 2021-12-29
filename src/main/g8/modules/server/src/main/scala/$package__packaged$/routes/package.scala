package $package$

import cats.effect.Async
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.headers.`Content-Type`
import org.http4s.{EntityDecoder, EntityEncoder, MediaType}

package object routes {
  implicit def deriveEntityEncoder[F[_]: Async, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

  implicit def deriveEntityDecoder[F[_]: Async, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

  def nameToContentType(filename: String): Option[`Content-Type`] =
    filename.lastIndexOf('.') match {
      case -1 => None
      case i  => MediaType.forExtension(filename.substring(i + 1)).map(`Content-Type`(_))
    }

  def getFileType(filename: String): String =
    filename.drop(filename.lastIndexOf(".") match {
      case -1 => filename.length
      case extensionStartIndex => extensionStartIndex
    })

}
