package $package$.domain.custom

import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.{MatchesRegex, Uri, Url}

package object refinements {
  type UriAddress = String Refined Uri
  object UriAddress extends RefinedTypeOps[UriAddress, String]

}