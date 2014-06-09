package documentation

import org.qirx.littlespec.fragments.Code
import org.qirx.littlespec.fragments.CompoundResult
import org.qirx.littlespec.fragments.Pending
import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.Text

import testUtils.ExampleUtils

object `1_GettingStarted` extends Specification with ExampleUtils { gettingStarted =>

  """|To create a specification, extend an object or class with `Specification`
       |and create a fragment. An empty fragment results in a `TODO`.""".stripMargin -
    new Example {
      import org.qirx.littlespec.Specification

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

  "An example showing multiple features" -
    new Example {
      import org.qirx.littlespec.Specification

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
