package org.qirx.littlespec.assertion

import org.qirx.littlespec.fragments.Fragment

trait FragmentBodyAssertEnhancement { self: StaticAssertions =>

  implicit class FragmentBodyEnhancement(body: => Fragment.Body) {

    def withMessage(changeMessage: String => String): Fragment.Body =
      try body
      catch {
        case Fragment.Failure(message) => failure(changeMessage(message))
      }

    def withMessage(message: String): Fragment.Body =
      withMessage(_ => message)
  }
}