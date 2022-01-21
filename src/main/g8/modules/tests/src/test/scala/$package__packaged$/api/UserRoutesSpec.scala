package $package$.api

import cats.effect.IO
import cats.implicits._
import $package$.domain.{Credentials, UserData}
import $package$.test.api.UserRoutesChecker
import $package$.test.utils._
import $package$.test.utils.logger.NoOp
import org.http4s._
import org.scalatest.Assertion

class UserRoutesSpec extends UserRoutesChecker[IO] {

  test("Register User") {
    def theTest(method: Method, body: Option[UserData] = None): IO[Assertion] = {
      val shouldReturn =
        if (method == Method.POST)
          if (body.nonEmpty) Status.Created
          else Status.BadRequest
        else Status.Unauthorized

      val params =
        s"""
        Params:
          Method: \$method
          Body: \$body
          Should Return: \$shouldReturn
      """
      reqToUserRegister(method, body)
        .map(res => assert(res.status == shouldReturn, params))
        .handleError { error =>
          fail(s"Test failed. Error: \$error")
        }
    }

    runAssertions(
      theTest(Method.POST),
      theTest(Method.GET),
      theTest(Method.POST, FakeData.userData.some),
      theTest(Method.POST, FakeData.userData.some)
    )
  }

  test("Authorization") {
    def theTest(isCorrect: Boolean, method: Method, body: Option[Credentials] = None): IO[Assertion] = {
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
          Method: \$method
          IsCorrectCredentials: \$isCorrect
          Body: \$body
          Should Return: \$shouldReturn
      """
      reqToAuth(method, body, isCorrect)
        .map(res => assert(res.status == shouldReturn, params))
        .handleError { error =>
          fail(s"Test failed. Error: \$error")
        }
    }

    runAssertions(
      theTest(isCorrect = false, Method.POST),
      theTest(isCorrect = false, Method.GET),
      theTest(isCorrect = false, Method.POST, FakeData.credentials(true).some),
      theTest(isCorrect = true, Method.POST),
      theTest(isCorrect = true, Method.GET),
      theTest(isCorrect = true, Method.POST, FakeData.credentials(true).some)
    )
  }

  test("GET User") {
    def theTest(isAuthed: Boolean, method: Method): IO[Assertion] = {
      val shouldReturn =
        if (isAuthed)
          if (method == Method.GET) Status.Ok
          else Status.NotFound
        else Status.Unauthorized

      val params =
        s"""
          Params:
            Method: \$method
            IsAuthorized: \$isAuthed
            Should Return: \$shouldReturn
        """
      reqToGetUser(method, isAuthed)
        .map(res => assert(res.status == shouldReturn, params))
        .handleError { error =>
          fail(s"Test failed. Error: \$error")
        }
    }

    runAssertions(
      theTest(isAuthed = false, Method.POST),
      theTest(isAuthed = false, Method.GET),
      theTest(isAuthed = true, Method.POST),
      theTest(isAuthed = true, Method.GET)
    )
  }

  test("Logout") {
    def theTest(isAuthed: Boolean, method: Method): IO[Assertion] = {
      val shouldReturn =
        if (isAuthed)
          if (method == Method.GET) Status.SeeOther
          else Status.NotFound
        else Status.Unauthorized

      val params =
        s"""
          Params:
            Method: \$method
            IsAuthorized: \$isAuthed
            Should Return: \$shouldReturn
        """
      reqToLogout(method, isAuthed)
        .map(res => assert(res.status == shouldReturn, params))
        .handleError { error =>
          fail(s"Test failed. Error: \$error")
        }
    }

    runAssertions(
      theTest(isAuthed = false, Method.POST),
      theTest(isAuthed = false, Method.GET),
      theTest(isAuthed = true, Method.POST),
      theTest(isAuthed = true, Method.GET)
    )
  }
}
