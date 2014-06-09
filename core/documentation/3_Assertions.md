**This documentation is generated from `documentation.3_Assertions`**

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
result1 failsWith message("CustomException")
result2 failsWith message("OtherCustomException")
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
