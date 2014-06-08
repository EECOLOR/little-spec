package testUtils.assertion

import org.qirx.littlespec.Specification
import testUtils.beAFailure

object NumericAssertEnhancementsSpec extends Specification with NumericAssertEnhancements {

  "NumericAssertEnhancements should" - {

    "add an isLessThan method" - {
      "that reports failure if the element is larger" - {
        (2 isLessThan 1) must beAFailure
      }
      "that reports success if the element is smaller" - {
        (1 isLessThan 2) is success
      }
    }

    "add an isMoreThan method" - {
      "that reports failure if the element is larger" - {
        (1 isMoreThan 2) must beAFailure
      }
      "that reports success if the element is smaller" - {
        (2 isMoreThan 1) is success
      }
    }
  }
}