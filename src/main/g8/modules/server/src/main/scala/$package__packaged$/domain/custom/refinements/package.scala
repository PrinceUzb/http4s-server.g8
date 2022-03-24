package $package$.domain.custom

import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.{MatchesRegex, Uri}

package object refinements {
  private type EmailPred = MatchesRegex["^[a-zA-Z0-9.]+@[a-zA-Z0-9]+\\\.[a-zA-Z]+\$"]

  type EmailAddress = String Refined EmailPred
  object EmailAddress extends RefinedTypeOps[EmailAddress, String]

  type UriAddress = String Refined Uri
  object UriAddress extends RefinedTypeOps[UriAddress, String]

}