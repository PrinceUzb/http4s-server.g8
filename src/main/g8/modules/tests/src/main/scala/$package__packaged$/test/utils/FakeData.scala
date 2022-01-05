package $package$.test.utils

import $package$.domain._
import $package$.domain.custom.refinements._
import eu.timepit.refined.auto.autoUnwrap

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Random

object FakeData {
  def randomString(length: Int): String = Random.alphanumeric.take(length).mkString

  def randomEmail: EmailAddress = EmailAddress.unsafeFrom(s"\${randomString(8)}@gmail.com")

  val Pass: Password = Password.unsafeFrom("Secret1!")

  def user(email: EmailAddress = randomEmail): User =
    User(
      id = UUID.randomUUID(),
      fullName = FullName.unsafeFrom("John Dao"),
      email = EmailAddress.unsafeFrom(email),
      createdAt = LocalDateTime.now
    )

  def userData: UserData =
    UserForm(
      fullName = FullName.unsafeFrom("John Dao"),
      email = randomEmail,
      password = Password.unsafeFrom("Secret1!")
    )
}