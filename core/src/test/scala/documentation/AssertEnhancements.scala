package documentation

import org.qirx.littlespec.assertion.Assertion
import org.qirx.littlespec.fragments.Fragment
import org.qirx.littlespec.Specification

import testUtils.FailWith
import testUtils.beAFailure
import testUtils.beAFailureWithMessage

object AssertEnhancements extends Specification {

  """|A few enhancements are added for every type, meaning that calling the
     |appropriate method results in an implicit resolution of that method""".stripMargin - {

    "The `is` enhancement performs a simple `==` comparison" - {

      "it results in a failure when the values are not equal" - {
        (1 is 2) must beAFailure
      }

      example {
        1 is 1
        "test" is "test"
        Seq(1, 2, 3) is List(1, 2, 3)
      }
    }

    "The `isLike` enhancement is added for every type and performs a pattern match" - {

      case class Test(value: String)

      def test(p: PartialFunction[Test, FragmentBody]) = Test("test") isLike p

      "it results in a failure when the partial function does not match" - {
        test { case Test("a string") => success } must beAFailure
      }

      "it returns the result of the partial function if it's matched" - {
        test { case Test(value) => value is "not test" } must beAFailure
      }

      example {
        Test("test") isLike {
          case Test(value) => value is "test"
        }
      }
    }

    """|The `must` enhancement is added for every type and allows you to
       |pass in an `Assertion` instance""".stripMargin - {

      "it results in a failure if the assertion returns a `Left(message)`" - {
        val assertion =
          new Assertion[String] {
            def assert(s: => String): Either[String, FragmentBody] =
              Left(s + " - failure")
          }

        ("test" must assertion) must beAFailureWithMessage("test - failure")
      }

      "it returns the body if the assertion returns a `Right(body)`" - {
        val assertion =
          new Assertion[String] {
            def assert(s: => String): Either[String, FragmentBody] =
              Right(todo)
          }
        ("test" must assertion) is todo
      }

      """|it accepts different assertion types as long as there is an
         |implicit conversion from `A` to `B`""".stripMargin - {

        val assertion =
          new Assertion[Seq[Char]] {
            def assert(s: => Seq[Char]): Either[String, FragmentBody] =
              Right(success)
          }

        ("test" must assertion) is success
      }

      example {
        val beTest =
          new Assertion[String] {
            def assert(s: => String) =
              if (s.isEmpty) Left("The string can not be empty")
              else Right(s is "test")
          }

        "test" must beTest
      }
    }

    "The `withMessage` enhancement allows you to change the message of a failure" - {

      example {
        def result =
          1 is 2 withMessage(_ + " - failed")

        result failsWith "1 is not equal to 2 - failed"
      }

      example {
        def result =
          1 is 2 withMessage("failed")

        result failsWith "failed"
      }
    }
  }
}