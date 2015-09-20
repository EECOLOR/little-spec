import org.qirx.littlespec.fragments.Fragment
import org.qirx.littlespec.assertion.BasicAssertEnhancements
import org.qirx.littlespec.assertion.StaticAssertions
import org.qirx.littlespec.assertion.ThrowingAssertions

package object testUtils {

  protected val assertUtils = new StaticAssertions with ThrowingAssertions with BasicAssertEnhancements

  import assertUtils._

  val beAFailure = throwA[Fragment.Failure]
  def beAFailureWithMessage(message: String) =
    beAFailure withMessage message

  implicit class FailWith(t: => Fragment.Body) {
    def failsWith(message: String) = t must beAFailureWithMessage(message)
  }

}