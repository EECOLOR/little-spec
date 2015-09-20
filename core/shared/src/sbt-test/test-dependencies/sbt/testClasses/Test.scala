import org.qirx.littlespec.Specification

object Test extends Specification {

  "Test should be able to" - {
    "run a test" - {
      1 + 1 is 2
    }
    "show an example" - {
      example {
        1 + 1 is 2
      }
    }
  }
}