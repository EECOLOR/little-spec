package documentation

import org.qirx.littlespec.fragments.Code
import org.qirx.littlespec.fragments.CompoundResult
import org.qirx.littlespec.fragments.Pending
import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.Text

import testUtils.{NoJSExportSpecification, ExampleUtils}

object `_1_GettingStarted` extends Specification with ExampleUtils { gettingStarted =>

  // override specification, otherwise scalajs will try to export nested objects which isn't supported
  // had to remove the import in the examples below for scalajs so this class gets used
  class Specification extends NoJSExportSpecification;

  """|To create a specification, extend an object or class with `Specification`
       |and create a fragment. An empty fragment results in a `TODO`.""".stripMargin -
    new Example {
      object ExampleSpec extends Specification {
        "implicit fragment" - {
          // fragment body
        }
      }
    }.expecting {
      _.ExampleSpec.executeFragments() is Seq(Pending(Text("implicit fragment"), "TODO"))
    }

  """|Fragments can be created in two ways, using the string enhancement or
       |by calling the `createFragment` method.""".stripMargin -
    new SpecificationExample {
      createFragment("explicit fragment", { /* fragment body */ })
    }.expecting {
      _ is Seq(todoResult("explicit fragment"))
    }

  "Fragments can be nested" -
    new SpecificationExample {
      "outer" - {
        "inner" - {
          // fragment body
        }
      }
    }.expecting {
      _ is Seq(CompoundResult(Text("outer"), Seq(todoResult("inner"))))
    }

  "Fragments can be disabled" -
    new SpecificationExample {
      "disabled" - {
        // fragment body
      }.disabled

      "disabled with a message" - {
        // fragment body
      }.disabled("message")
    }.expecting {
      _ is Seq(
        Pending(Text("disabled"), "DISABLED"),
        Pending(Text("disabled with a message"), "message"))
    }

  "An example showing multiple features" -
    new Example {
      object ExampleSpec extends Specification {

        trait Adder[T] {
          def add(a: T, b: T): T
        }
        object Adder {
          implicit def numericAdder[T: Numeric] =
            new Adder[T] {
              def add(a: T, b: T) = implicitly[Numeric[T]].plus(a, b)
            }
          implicit def stringAdder =
            new Adder[String] {
              def add(a: String, b: String) = a + b
            }
          implicit def booleanAdder =
            new Adder[Boolean] {
              def add(a: Boolean, b: Boolean) = sys.error("Can not add booleans")
            }
        }

        def add[T](a: T, b: T)(implicit adder: Adder[T]) = adder.add(a, b)

        "The `add` method should" - {

          "exist" - {
            add(1, 2)
            success
          }

          "perform a basic addition on numbers" - {
            add(1, 2) is 3
            add(1L, 2L) is 3L
          }

          "be able to concatenate strings" - {
            add("one", "two") is "onetwo"
          }

          "have a method to add unicorns" - {
            pending("The rainbows are not ready yet")
          }

          example {
            add(1, 2) is 3
          }

          "fail for booleans" - {
            add(true, false) must throwA[RuntimeException]
              .withMessage("Can not add booleans")
          }
        }
      }
    }.expecting {
      _.ExampleSpec.executeFragments() isLike {

        case Seq(
          CompoundResult(Text("The `add` method should"), Seq(
            Success(Text("exist")),
            Success(Text("perform a basic addition on numbers")),
            Success(Text("be able to concatenate strings")),
            Pending(Text("have a method to add unicorns"), "The rainbows are not ready yet"),
            Success(Code("add(1, 2) is 3")),
            Success(Text("fail for booleans"))))) => success
      }
    }

  def todoResult(title: String) = Pending(Text(title), "TODO")
}
