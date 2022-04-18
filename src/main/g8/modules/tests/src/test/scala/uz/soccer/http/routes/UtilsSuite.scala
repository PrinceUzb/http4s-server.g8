package $package$.http.routes

import eu.timepit.refined.cats.refTypeShow
import org.http4s.MediaType.allMediaTypes
import $package$.utils.Generators.filenameGen
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers

object UtilsSuite extends SimpleIOSuite with Checkers {
  test("Retrieve content type") {
    forall(filenameGen) { filename =>
      expect(nameToContentType(filename).isDefined)
    }
  }

  test("Retrieve file type") {
    forall(filenameGen) { filename =>
      expect(allMediaTypes.flatMap(_.fileExtensions).contains(getFileType(filename)))
    }
  }
}
