package testUtils.assertion

import org.qirx.littlespec.assertion.StaticAssertions

trait NumericAssertEnhancements { self: StaticAssertions =>

  implicit class NummericAssertEnhancements[A](result: => A) {

    def isLessThan[T >: A <: A](other: => T)(implicit numeric: Numeric[T]) =
      if (numeric.lt(result, other)) success
      else failure(result + " is not lesser than " + other)

    def isMoreThan[T >: A <: A](other: => T)(implicit numeric: Numeric[T]) =
      if (numeric.gt(result, other)) success
      else failure(result + " is not lesser than " + other)
  }
}