package $package$
import cats.implicits.toContravariantOps
import cats.{Eq, Show}
import dev.profunktor.auth.jwt.JwtToken
import io.circe.Encoder

package object domain {
  implicit val tokenEq: Eq[JwtToken] = Eq.by(_.value)

  implicit val tokenShow: Show[JwtToken] = Show[String].contramap[JwtToken](_.value)

  implicit val tokenEncoder: Encoder[JwtToken] =
    Encoder.forProduct1("access_token")(_.value)
}
