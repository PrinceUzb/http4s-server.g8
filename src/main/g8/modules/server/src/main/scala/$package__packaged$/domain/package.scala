package $package$
import cats.implicits.toContravariantOps
import cats.{Eq, Show}
import dev.profunktor.auth.jwt.JwtToken
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import java.time.LocalDateTime

package object domain {
  implicit val tokenEq: Eq[JwtToken] = Eq.by(_.value)

  implicit val tokenShow: Show[JwtToken] = Show[String].contramap[JwtToken](_.value)

  implicit val tokenEncoder: Encoder[JwtToken] = deriveEncoder

  implicit val javaTimeShow: Show[LocalDateTime] = Show[String].contramap[LocalDateTime](_.toString)
}
