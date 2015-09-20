package org.qirx.littlespec.assertion

import org.qirx.littlespec.fragments.Fragment

trait Assertion[T] {
  def assert(s: => T): Either[String, Fragment.Body]
}