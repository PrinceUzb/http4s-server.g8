package $package$.domain.custom.exception
import $package$.domain.custom.refinements.EmailAddress

import scala.util.control.NoStackTrace

case class UserNotFound(email: EmailAddress) extends NoStackTrace
