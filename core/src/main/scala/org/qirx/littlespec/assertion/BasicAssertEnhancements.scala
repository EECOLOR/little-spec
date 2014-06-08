package org.qirx.littlespec.assertion

import org.qirx.littlespec.fragments.Fragment

trait BasicAssertEnhancements { self: StaticAssertions =>

  implicit class IsEnhancement[A](result: => A) {
    def is[B](expected: => B): Fragment.Body = {
      if (result != expected) failure(result + " is not equal to " + expected)
      else success
    }
  }

  implicit class MustEnhancement[A](result: => A) {
    def must[B](assertion: => Assertion[B])(implicit ev: A => B): Fragment.Body =
      assertion.assert(result) match {
        case Right(body) => body
        case Left(message) => failure(message)
      }
  }

  implicit class IsLikeEnhancement[A](result: => A) {
    def isLike(matcher: PartialFunction[A, Fragment.Body]): Fragment.Body =
      if (matcher.isDefinedAt(result)) matcher(result)
      else failure(result + " does not match pattern")
  }
}