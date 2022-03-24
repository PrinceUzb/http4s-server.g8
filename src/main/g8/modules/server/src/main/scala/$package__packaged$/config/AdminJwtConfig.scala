package $package$.config

import ciris.Secret
import $package$.types.{AdminUserTokenConfig, JwtSecretKeyConfig}

case class AdminJwtConfig(
  secretKey: Secret[JwtSecretKeyConfig],
  adminToken: Secret[AdminUserTokenConfig]
)
