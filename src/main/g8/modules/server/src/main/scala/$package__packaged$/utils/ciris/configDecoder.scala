package $package$.utils.ciris

import _root_.ciris.ConfigDecoder
import $package$.utils.derevo.Derive

object configDecoder extends Derive[Decoder.Id]

object Decoder {
  type Id[A] = ConfigDecoder[String, A]
}
