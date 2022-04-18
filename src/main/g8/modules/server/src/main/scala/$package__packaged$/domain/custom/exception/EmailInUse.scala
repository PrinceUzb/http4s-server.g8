package $package$.domain.custom.exception

import $package$.domain.custom.refinements.EmailAddress
import scala.util.control.NoStackTrace

case class EmailInUse(email: EmailAddress) extends NoStackTrace
