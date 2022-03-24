package $package$.config

import scala.concurrent.duration.FiniteDuration

case class HttpClientConfig(
    timeout: FiniteDuration,
    idleTimeInPool: FiniteDuration
)