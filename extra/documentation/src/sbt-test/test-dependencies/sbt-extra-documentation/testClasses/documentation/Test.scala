package documentation

import org.qirx.littlespec.Specification

object Test extends Specification {

  "#Title" - {
    "test" - {
      1 + 1 is 2
    }
    "example" - {
      example {
        1 + 1 is 2
      }
    }
  }
}