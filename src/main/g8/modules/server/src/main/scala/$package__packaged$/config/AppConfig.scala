package $package$.config

case class AppConfig(
  dbConfig: DBConfig,
  logConfig: LogConfig,
  serverConfig: HttpServerConfig,
  redisConfig: RedisConfig
)