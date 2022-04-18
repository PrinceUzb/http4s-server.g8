package $package$.utils

import eu.timepit.refined.scalacheck.string._
import eu.timepit.refined.types.string.NonEmptyString
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import $package$.domain.User._
import $package$.domain.custom.refinements.{EmailAddress, FileName, Password}
import $package$.domain.types.{UserId, UserName}
import $package$.domain.{Credentials, Gender, Role, User}
import $package$.utils.Arbitraries._

import java.util.UUID

object Generators {

  def nonEmptyStringGen(min: Int, max: Int): Gen[String] =
    Gen
      .chooseNum(min, max)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaChar)
      }

  def nonEmptyAlphaNumGen(min: Int, max: Int): Gen[String] =
    Gen
      .chooseNum(min, max)
      .flatMap { n =>
        Gen.buildableOfN[String, Char](n, Gen.alphaNumChar)
      }

  def idGen[A](f: UUID => A): Gen[A] =
    Gen.uuid.map(f)

  val userIdGen: Gen[UserId] =
    idGen(UserId.apply)

  val usernameGen: Gen[UserName] =
    arbitrary[NonEmptyString].map(UserName.apply)

  val passwordGen: Gen[Password] = arbitrary[Password]

  val booleanGen: Gen[Boolean] = arbitrary[Boolean]

  val emailGen: Gen[EmailAddress] = arbitrary[EmailAddress]

  val filenameGen: Gen[FileName] = arbitrary[FileName]

  val genderGen: Gen[Gender] = arbitrary[Gender]

  val roleGen: Gen[Role] = arbitrary[Role]

  val userGen: Gen[User] =
    for {
      i <- userIdGen
      n <- usernameGen
      e <- emailGen
      g <- genderGen
      r <- roleGen
    } yield User(i, n, e, g, r)

  val userCredentialGen: Gen[Credentials] =
    for {
      e <- emailGen
      p <- passwordGen
    } yield Credentials(e, p)

  val createUserGen: Gen[CreateUser] =
    for {
      u <- usernameGen
      e <- emailGen
      g <- genderGen
      p <- passwordGen
    } yield CreateUser(u, e, g, p)
}
