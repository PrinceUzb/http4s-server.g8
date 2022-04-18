package $package$.domain
import cats.Show
import io.circe.{Decoder, Encoder}

sealed abstract class Gender(val value: String)

object Gender {
  case object MALE   extends Gender("male")
  case object FEMALE extends Gender("female")

  val genders = List(MALE, FEMALE)

  def find(value: String): Option[Gender] =
    genders.find(_.value == value.toLowerCase)

  def unsafeFrom(value: String): Gender =
    find(value).getOrElse(throw new IllegalArgumentException(s"value doesn't match [ \$value ]"))

  implicit val encStatus: Encoder[Gender] = Encoder.encodeString.contramap[Gender](_.value)
  implicit val decStatus: Decoder[Gender] = Decoder.decodeString.map(unsafeFrom)
  implicit val show: Show[Gender] = Show.show(_.value)

}
