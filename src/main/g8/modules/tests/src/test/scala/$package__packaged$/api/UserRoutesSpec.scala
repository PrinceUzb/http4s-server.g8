package $package$.api

import cats.data.NonEmptyList
import cats.effect.IO
import cats.implicits.*
import $package$.domain.{EmailAndPassword, UserData}
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.Encoder
import org.http4s.*
import test.crowdlabel.api.UserRoutesChecker
import test.crowdlabel.utils.{FakeData, TestEnv}
import test.crowdlabel.utils.logger.NoOp

class UserRoutesSpec extends TestEnv with UserRoutesChecker[IO] {

  test("Register User") {
    def theTest(isCorrect: Boolean, method: Method, body: Option[UserData] = None) = {
      val shouldReturn =
        if (method == Method.POST)
          if (body.nonEmpty && isCorrect) Status.NoContent
          else Status.BadRequest
        else Status.Unauthorized

      val params =
        s"""
        Params:
          Method: $method
          IsCorrectCredentials: $isCorrect
          Body: $body
          Should Return: $shouldReturn
      """
      reqToUserRegister(method, body, isCorrect)
        .map(res => assert(res.status == shouldReturn, params))
        .handleError { error =>
          fail(s"Test failed. Error: $error")
        }
    }

    runAssertions(
      theTest(false, Method.POST),
      theTest(false, Method.GET),
      theTest(false, Method.POST, FakeData.userForm("expired").some),
      theTest(false, Method.POST, FakeData.userForm().some),
      theTest(true, Method.POST),
      theTest(true, Method.GET),
      theTest(true, Method.POST, FakeData.userForm().some),
    )
  }

  test("Authorization") {
    def theTest(isCorrect: Boolean, method: Method, body: Option[EmailAndPassword] = None) = {
      val shouldReturn =
        if (method == Method.POST)
          if (body.nonEmpty)
            if (isCorrect) Status.NoContent
            else Status.Forbidden
          else Status.BadRequest
        else Status.Unauthorized
      val params =
        s"""
        Params:
          Method: $method
          IsCorrectCredentials: $isCorrect
          Body: $body
          Should Return: $shouldReturn
      """
      reqToAuth(method, body, isCorrect)
        .map(res => assert(res.status == shouldReturn, params))
        .handleError { error =>
          fail(s"Test failed. Error: $error")
        }
    }

    val body = EmailAndPassword(EmailAddress.unsafeFrom(FakeData.randomEmail(4)), Password.unsafeFrom("Secret1!")).some

    runAssertions(
      theTest(false, Method.POST),
      theTest(false, Method.GET),
      theTest(false, Method.POST, body),
      theTest(true, Method.POST),
      theTest(true, Method.GET),
      theTest(true, Method.POST, body)
    )
  }

  test("GET User") {
    def theTest(isAuthed: Boolean, method: Method) = {
      val shouldReturn =
        if (isAuthed)
          if (method == Method.GET)
            Status.Ok
          else
            Status.NotFound
        else
          Status.Unauthorized

      val params =
        s"""
          Params:
            Method: $method
            IsAuthorized: $isAuthed
            Should Return: $shouldReturn
        """
      reqToGetUser(method, isAuthed)
        .map(res => assert(res.status == shouldReturn, params))
        .handleError { error =>
          fail(s"Test failed. Error: $error")
        }
    }

    runAssertions(
      theTest(false, Method.POST),
      theTest(false, Method.GET),
      theTest(true, Method.POST),
      theTest(true, Method.GET),
    )
  }
}