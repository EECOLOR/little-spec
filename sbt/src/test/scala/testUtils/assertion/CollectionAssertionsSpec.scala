package testUtils.assertion

import org.qirx.littlespec.Specification
import testUtils.assertion.CollectionAssertions

object CollectionAssertionsSpec extends Specification with CollectionAssertions {

  "CollectionAssertions should" - {

    "provide a 'contain' assertion for iterables" - {

      "that fails if the given array does not contain the element" - {
        val assertion = contain { case s: String => }
        assertion.assert(Array(1)) is
          Left("Could not find element that matches the given pattern")
      }

      "that runs the content of the function if it does contain the element" - {
        val assertion = contain { case s: String => pending("test") }
        assertion.assert(Seq("")) is
          Right(pending("test"))
      }

      "that works with more than one element" - {
        val assertion = contain { case s: String => s is "test" }
        assertion.assert(Seq("wrong", "test")) is Right(success)
        assertion.assert(Seq("test", "wrong")) is Right(success)
      }

      "that allows exceptions to escape" - {
        val assertion = contain { case s: String => throw new Throwable {} }
        assertion.assert(Seq("wrong", "test")) must throwA[Throwable]
      }

      "that fails when assertion on all elements fails" - {
        val assertion = contain { case s: String => s is "not here" }
        assertion.assert(Seq("test1", "test2")) is
          Left("Could not find element, last failure: test2 is not equal to not here")
      }
    }
  }
}