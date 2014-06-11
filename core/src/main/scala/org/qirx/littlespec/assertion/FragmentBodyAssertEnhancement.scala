package org.qirx.littlespec.assertion

import org.qirx.littlespec.fragments.Fragment

trait FragmentBodyAssertEnhancement { self: StaticAssertions =>

  implicit class FragmentBodyEnhancement[T](body: => T)(implicit asBody: T => Fragment.Body) {

    def withMessage(changeMessage: String => String): Fragment.Body =
      try body
      catch {
        case Fragment.Failure(message) => failure(changeMessage(message))
      }

    def withMessage(message: String): Fragment.Body =
      withMessage(_ => message)

    def disabled(message: String): Fragment.Body = pending(message)
    val disabled: Fragment.Body = disabled("DISABLED")
  }
}