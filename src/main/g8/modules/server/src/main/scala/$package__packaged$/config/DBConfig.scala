package $package$.config

import ciris.Secret
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.types.string.NonEmptyString

case class DBConfig(
  host: NonEmptyString,
  port: UserPortNumber,
  user: NonEmptyString,
  password: Secret[NonEmptyString],
  database: NonEmptyString,
  poolSize: PosInt
)
