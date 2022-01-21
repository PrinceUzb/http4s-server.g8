package $package$.test.utils

import cats.effect.IO
import cats.implicits._
import $package$.test.utils.IOAssertion
import org.scalatest.Checkpoints.Checkpoint
import org.scalatest.{Assertion, BeforeAndAfterAll}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

trait TestEnv extends AnyFunSuite with ScalaCheckDrivenPropertyChecks with CatsEquality with BeforeAndAfterAll {
  def runAssertions(ioAssertions: IO[Assertion]*): Unit = {
    val cp = new Checkpoint()

    IOAssertion {
      ioAssertions
        .toList
        .traverse(identity)
        .map { ts =>
          ts.foreach(cp(_))
          cp.reportAll()
        }
    }
  }
}