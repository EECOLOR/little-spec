**This documentation is generated from `documentation._1_GettingStarted`**

---
To create a specification, extend an object or class with `Specification`
and create a fragment. An empty fragment results in a `TODO`.
`Specification` is a trait, so you can also use it as a mixin if needed.
```scala
object ExampleSpec extends Specification {
  "implicit fragment" - {
    // fragment body
  }
}
```
Fragments can be created in two ways, using the string enhancement or
by calling the `createFragment` method.
```scala
createFragment("explicit fragment", { /* fragment body */ })
```
Fragments can be nested
```scala
"outer" - {
  "inner" - {
    // fragment body
  }
}
```
Fragments can be disabled
```scala
"disabled" - {
  // fragment body
}.disabled

"disabled with a message" - {
  // fragment body
}.disabled("message")
```
An example showing multiple features
```scala
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
```
