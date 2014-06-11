package org.qirx.littlespec.assertion

import scala.reflect.ClassTag
import org.qirx.littlespec.fragments.Fragment

trait ThrowingAssertions { self: StaticAssertions with BasicAssertEnhancements =>

  def throwA[E <: Throwable: ClassTag]: ExceptionMatcher[E] =
    new ExceptionMatcher[E]
  def throwAn[E <: Throwable: ClassTag]: ExceptionMatcher[E] =
    throwA[E]

  class ExceptionMatcher[E <: Throwable : ClassTag] extends Assertion[Any] {

    def assert(code: => Any) =
      runCode(code, _ => success)

    def like(handler: E => Fragment.Body): Assertion[Any] =
      new Assertion[Any] {
        def assert(code: => Any) = runCode(code, handler)
      }

    def withMessage(message: String): Assertion[Any] =
      like(e => e.getMessage is message)

    private def runCode(code: => Any, handler: E => Fragment.Body) =
      try {
        code
        val expectedClass = implicitly[ClassTag[E]].runtimeClass
        val expectedName = expectedClass.getName
        Left(s"Expected '$expectedName' but no exception was thrown")
      } catch {
        case e: E => Right(handler(e))
      }
  }
}