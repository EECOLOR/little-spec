package org.qirx.littlespec.fragments

import scala.concurrent.duration.FiniteDuration

sealed trait Result {
  def title: Title
}

case class Pending(title: Title, message: String) extends Result

// duration is in the second parameter list to make pattern matching easier
case class Success(title: Title)(val duration: FiniteDuration) extends Result

case class UnexpectedFailure(title: Title, throwable: Throwable) extends Result

case class Failure(title: Title, message: String, failure: Fragment.Failure) extends Result

case class CompoundResult(title: Title, results: Seq[Result]) extends Result
