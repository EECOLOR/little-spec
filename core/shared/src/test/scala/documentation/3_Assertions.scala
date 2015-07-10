package documentation

import org.qirx.littlespec.fragments.Failure
import org.qirx.littlespec.fragments.Fragment
import org.qirx.littlespec.fragments.Pending
import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.Text

import testUtils.ExampleUtils
import testUtils.FailWith

object `_3_Assertions` extends Specification with ExampleUtils { self =>

  "The simplest form of assertions are the static assertions" - {
    new SpecificationExample {
      "todo" - {
        todo
      }

      "success" - {
        success
      }

      "failure" - {
        failure("message")
      }

      "pending" - {
        pending("message")
      }
    }.expecting {
      _ isLike {
        case Seq(
          Pending(Text("todo"), "TODO"),
          Success(Text("success")),
          Failure(Text("failure"), "message", Fragment.Failure("message")),
          Pending(Text("pending"), "message")) => success
      }
    }
  }

  "There are different assertions available that work with the `must` enhancement" - {

    "The `throwA` and `throwAn` assertions expect an exception to be thrown" - {

      example {
        def result1 = 1 must throwA[CustomException]
        def result2 = 2 must throwAn[OtherCustomException]

        def message(name: String) = s"Expected '$name' but no exception was thrown"
        result1 failsWith message("documentation.CustomException")
        result2 failsWith message("documentation.OtherCustomException")
      }

      example {
        def code1: Any = throw new CustomException
        def code2: Any = throw new OtherCustomException

        def result1 = code1 must throwA[CustomException]
        def result2 = code2 must throwAn[OtherCustomException]

        result1 is success
        result2 is success
      }

      "If another exception is thrown it is ignored" - {
        def code: Any = throw new RuntimeException
        (code must throwA[CustomException]) must throwA[RuntimeException]
        (code must throwAn[OtherCustomException]) must throwA[RuntimeException]
      }

      """|This assertion can be made more specific with the `like` method. If no
         |exception was thrown it will behave the same when no exception is thrown.""".stripMargin - {

        def result1 = 1 must throwA[CustomException].like {
          case CustomException(value) => todo
        }
        def result2 = 2 must throwAn[OtherCustomException].like {
          case OtherCustomException(value) => todo
        }

        def message(name: String) = s"Expected '$name' but no exception was thrown"
        result1 failsWith message("documentation.CustomException")
        result2 failsWith message("documentation.OtherCustomException")
      }

      example {
        def code1: Any = throw CustomException("test1")
        def code2: Any = throw OtherCustomException("test2")

        def result1 =
          code1 must throwA[CustomException].like {
            case CustomException(message) => pending(message)
          }
        def result2 =
          code2 must throwAn[OtherCustomException].like {
            case OtherCustomException(message) => pending(message)
          }

        result1 is pending("test1")
        result2 is pending("test2")
      }

      "It also allows you to check the message of an exception" - example {
        def code1: Any = throw CustomException("test1")
        def code2: Any = throw OtherCustomException("test2")

        def result1 = code1 must throwA[CustomException].withMessage("test1")
        def result2 = code2 must throwAn[OtherCustomException].withMessage("test2")

        result1 is success
        result2 is success
      }
    }

    "The `beAnInstanceOf` assertion expects an instance to be of a given type" - {

      example {
        def result1 = "string" must beAnInstanceOf[String]
        def result2 = new CustomInstance must beAnInstanceOf[CustomType]

        result1 is success
        result2 is success
      }

      "It fails when the instance if not of the correct type" - {
        1 must beAnInstanceOf[String] failsWith
          "java.lang.Integer is not an instance of java.lang.String"

        new CustomInstance must beAnInstanceOf[OtherCustomType] failsWith
          "documentation.CustomInstance is not an instance of documentation.OtherCustomType"
      }
    }

  }
}

trait CustomType
trait OtherCustomType
class CustomInstance extends CustomType

case class CustomException(message: String = "") extends Throwable(message)
case class OtherCustomException(message: String = "") extends Throwable(message)