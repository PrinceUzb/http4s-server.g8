package $package$.utils
import eu.timepit.refined.scalacheck.string._
import eu.timepit.refined.types.string.NonEmptyString
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import $package$.domain.auth._
import $package$.http.auth.users.User

import java.util.UUID

object Generators {

  val nonEmptyStringGen: Gen[String] =
    Gen
      .chooseNum(21, 40)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }


  val booleanGen: Gen[Boolean] =
    Gen.oneOf(true, false)

  def nesGen[A](f: String => A): Gen[A] =
    nonEmptyStringGen.map(f)

  def idGen[A](f: UUID => A): Gen[A] =
    Gen.uuid.map(f)

  val userIdGen: Gen[UserId] =
    idGen(UserId.apply)

  val userNameGen: Gen[UserName] =
    nesGen(UserName.apply)

  val userNameParamGen: Gen[UserNameParam] =
    arbitrary[NonEmptyString].map(UserNameParam.apply)

  val passwordParamGen: Gen[PasswordParam] =
    arbitrary[NonEmptyString].map(PasswordParam.apply)

  val userGen: Gen[User] =
    for {
      i <- userIdGen
      n <- userNameGen
    } yield User(i, n)

  val userCredentialGen: Gen[LoginUser] =
    for {
      n <- userNameParamGen
      p <- passwordParamGen
    } yield LoginUser(n, p)
}
