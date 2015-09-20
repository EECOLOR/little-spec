package documentation

import org.qirx.littlespec.assertion.Assertion
import org.qirx.littlespec.fragments.Code
import org.qirx.littlespec.fragments.CompoundResult
import org.qirx.littlespec.fragments.Fragment
import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.Text
import org.qirx.littlespec.io.Source
import org.qirx.littlespec.macros.Location

import testUtils.ExampleUtils
import testUtils.FailWith

object `_5_Customization` extends Specification with ExampleUtils {
  "There are a lot of ways in which you can make things more readable and usable" - {

    """|The easiest form of customization is the use of `Assertion` classes
       |that work with the `must` enhancement.""".stripMargin -
      example {
        val beAnElephant =
          new Assertion[String /* this assertions only works on strings */ ] {
            def assert(s: => String) =
              if (s != "elephant") Left("The given string is not an elephant")
              else Right(success)
          }

        "elephant" must beAnElephant
        ("mouse" must beAnElephant) failsWith "The given string is not an elephant"
      }

    "You could also rename or combine existing assertions" -
      example {
        def beAFailure = throwA[Fragment.Failure]
        def beAFailureWithMessage(message: String) = beAFailure withMessage message

        failure("test") must beAFailureWithMessage("test")
      }

    "Another form is by using enhancements" - {

      example {
        implicit class FailWith(t: => FragmentBody) {
          def failsWith(message: String) =
            t must (throwA[Fragment.Failure] withMessage message)
        }

        failure("test") failsWith "test"
      }

      example {
        implicit class IntAssertEnhancement(i: Int) {
          def isThree = i is 3
        }

        3.isThree
      }
    }

    "It's also possible to use the source code in custom fragments" - {

      new SpecificationExample {
        import org.qirx.littlespec.io.Source
        import org.qirx.littlespec.macros.Location

        // Make sure the location is passed in implicitly,
        // this allows the macro to materialize it.
        class Example(implicit location: Location) { self =>

          // Source.codeAtLocation will grab the source code
          // between { and }
          def expecting(result: self.type => FragmentBody) =
            createFragment(Source.codeAtLocation(location), result(self))
        }

        trait MyLibraryTrait {
          def name: String
        }

        // Usage example:

        "API documentation" -
          new Example {
            // Extend the library trait and implement the name property
            object CustomObject extends MyLibraryTrait {
              lazy val name = "test"
            }
          }.expecting {
            _.CustomObject.name is "test"
          }

      }.expecting {
        val Expected =
          """|// Extend the library trait and implement the name property
             |object CustomObject extends MyLibraryTrait {
             |  lazy val name = "test"
             |}""".stripMargin
        _ isLike {
          case Seq(
            CompoundResult(Text("API documentation"), Seq(
              Success(Code(Expected))))) => success
        }
      }
    }
  }
}