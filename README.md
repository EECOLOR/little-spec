*This file is generated using sbt*

Little Spec
===========

A small test framework for [sbt](http://www.scala-sbt.org/) and [Scala.js](http://www.scala-js.org/) that allows you to write specifications.

Installation
============

sbt
---

Add the framework as library dependency

```scala
libraryDependencies += "org.qirx" %% "little-spec" % "0.4" % "test"
```

Add the test framework

```scala
testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")
```

Scala.js
---

Add the framework as library dependency

```scala
libraryDependencies += "org.qirx" %%% "little-spec" % "0.4" % "test"
```

Add the test framework

```scala
ScalaJSKeys.scalaJSTestFramework in Test := "org.qirx.littlespec.scalajs.TestFramework"
```

Usage
=====

**This documentation is generated from `documentation._1_GettingStarted`**

---
To create a specification, extend an object or class with `Specification`
and create a fragment. An empty fragment results in a `TODO`.
```scala
import org.qirx.littlespec.Specification

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
```


**This documentation is generated from `documentation._2_AssertEnhancements`**

---
A few enhancements are added for every type, meaning that calling the
appropriate method results in an implicit resolution of that method
The `is` enhancement performs a simple `==` comparison
it results in a failure when the values are not equal
```scala
1 is 1
"test" is "test"
Seq(1, 2, 3) is List(1, 2, 3)
```
The `isLike` enhancement is added for every type and performs a pattern match
it results in a failure when the partial function does not match
it returns the result of the partial function if it's matched
```scala
Test("test") isLike {
  case Test(value) => value is "test"
}
```
The `must` enhancement is added for every type and allows you to
pass in an `Assertion` instance
it results in a failure if the assertion returns a `Left(message)`
it returns the body if the assertion returns a `Right(body)`
it accepts different assertion types as long as there is an
implicit conversion from `A` to `B`
```scala
val beTest =
  new Assertion[String] {
    def assert(s: => String) =
      if (s.isEmpty) Left("The string can not be empty")
      else Right(s is "test")
  }

"test" must beTest
```
The `withMessage` enhancement allows you to change the message of a failure
```scala
def result =
  1 is 2 withMessage(_ + " - failed")

result failsWith "1 is not equal to 2 - failed"
```
```scala
def result =
  1 is 2 withMessage("failed")

result failsWith "failed"
```


**This documentation is generated from `documentation._3_Assertions`**

---
The simplest form of assertions are the static assertions
```scala
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
```
There are different assertions available that work with the `must` enhancement
The `throwA` and `throwAn` assertions expect an exception to be thrown
```scala
def result1 = 1 must throwA[CustomException]
def result2 = 2 must throwAn[OtherCustomException]

def message(name: String) = s"Expected '$name' but no exception was thrown"
result1 failsWith message("documentation.CustomException")
result2 failsWith message("documentation.OtherCustomException")
```
```scala
def code1: Any = throw new CustomException
def code2: Any = throw new OtherCustomException

def result1 = code1 must throwA[CustomException]
def result2 = code2 must throwAn[OtherCustomException]

result1 is success
result2 is success
```
If another exception is thrown it is ignored
This assertion can be made more specific with the `like` method. If no
exception was thrown it will behave the same when no exception is thrown.
```scala
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
```
It also allows you to check the message of an exception
```scala
def code1: Any = throw CustomException("test1")
def code2: Any = throw OtherCustomException("test2")

def result1 = code1 must throwA[CustomException].withMessage("test1")
def result2 = code2 must throwAn[OtherCustomException].withMessage("test2")

result1 is success
result2 is success
```
The `beAnInstanceOf` assertion expects an instance to be of a given type
```scala
def result1 = "string" must beAnInstanceOf[String]
def result2 = new CustomInstance must beAnInstanceOf[CustomType]

result1 is success
result2 is success
```
It fails when the instance if not of the correct type


**This documentation is generated from `documentation._4_ExampleFragments`**

---
Sometimes you want to show how your library is used, the problem with
documenting code examples is that they 'rot'. In case you change your
library you will not be notified of any compile errors in your
documentation. On top of that, the code might not be doing what you
inteded. The `example` fragment helps you in these cases.
```scala
def specialMethod(x:Int) = 1 + x

example {
  def result = specialMethod(1)
  result is 2
}
```


**This documentation is generated from `documentation._5_Customization`**

---
There are a lot of ways in which you can make things more readable and usable
The easiest form of customization is the use of `Assertion` classes
that work with the `must` enhancement.
```scala
val beAnElephant =
  new Assertion[String /* this assertions only works on strings */ ] {
    def assert(s: => String) =
      if (s != "elephant") Left("The given string is not an elephant")
      else Right(success)
  }

"elephant" must beAnElephant
("mouse" must beAnElephant) failsWith "The given string is not an elephant"
```
You could also rename or combine existing assertions
```scala
def beAFailure = throwA[Fragment.Failure]
def beAFailureWithMessage(message: String) = beAFailure withMessage message

failure("test") must beAFailureWithMessage("test")
```
Another form is by using enhancements
```scala
implicit class FailWith(t: => FragmentBody) {
  def failsWith(message: String) =
    t must (throwA[Fragment.Failure] withMessage message)
}

failure("test") failsWith "test"
```
```scala
implicit class IntAssertEnhancement(i: Int) {
  def isThree = i is 3
}

3.isThree
```
It's also possible to use the source code in custom fragments
```scala
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

```
