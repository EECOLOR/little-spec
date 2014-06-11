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
