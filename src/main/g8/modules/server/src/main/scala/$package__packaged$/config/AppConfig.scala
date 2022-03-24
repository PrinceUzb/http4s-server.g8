package $package$.config

case class AppConfig(
  adminJwtConfig: AdminJwtConfig,
  jwtConfig: JwtConfig,
  dbConfig: DBConfig,
  redis: RedisConfig,
  serverConfig: HttpServerConfig,
  logConfig: LogConfig
)
