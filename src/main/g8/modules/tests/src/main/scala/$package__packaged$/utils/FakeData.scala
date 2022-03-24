package $package$.utils

import $package$.domain.custom.refinements._

import scala.util.Random

object FakeData {
  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def randomEmail: EmailAddress = EmailAddress.unsafeFrom(s"\${randomString(8)}@gmail.com")

}
