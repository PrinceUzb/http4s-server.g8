package $package$.config

import ciris.Secret
import $package$.types.{JwtAccessTokenKeyConfig, PasswordSalt, TokenExpiration}

case class JwtConfig(
  tokenConfig: Secret[JwtAccessTokenKeyConfig],
  passwordSalt: Secret[PasswordSalt],
  tokenExpiration: TokenExpiration
)
