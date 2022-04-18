package $package$.config

case class AppConfig(
  jwtConfig: JwtConfig,
  dbConfig: DBConfig,
  redis: RedisConfig,
  serverConfig: HttpServerConfig,
  logConfig: LogConfig
)
