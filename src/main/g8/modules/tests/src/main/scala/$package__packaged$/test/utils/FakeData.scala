package test.crowdlabel.utils

import cats.data.NonEmptyList
import cats.implicits.*
import crowdlabel.domain.*
import crowdlabel.domain.AnswerType.*
import crowdlabel.domain.Project.{TaskType, ProjectStatus}
import crowdlabel.domain.custom.refinements.*
import eu.timepit.refined.auto.autoUnwrap
import eu.timepit.refined.types.numeric.*
import eu.timepit.refined.types.string.NonEmptyString

import java.time.LocalDateTime
import java.util.UUID
import scala.util.Random

object FakeData:
  val Pass: Password = Password.unsafeFrom("Secret1!")

  def user(emailAddress: EmailAddress, rateId: UUID): User =
    User(
      id = UUID.randomUUID(),
      fullName = FullName.unsafeFrom("John Dao"),
      passwordExpiresAt = LocalDateTime.now,
      createdAt = LocalDateTime.now,
      updatedAt = LocalDateTime.now.some,
      email = EmailAddress.unsafeFrom(emailAddress),
      rateId = rateId
    )

  def userForm(invite: String = "aaa"): UserForm =
    UserForm(
      fullName = FullName.unsafeFrom("John Dao"),
      email = EmailAddress.unsafeFrom(randomEmail(4)),
      inviteCode = NonEmptyString.unsafeFrom(invite),
      password = Password.unsafeFrom("Secret1!")
    )