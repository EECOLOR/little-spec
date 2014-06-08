// format: +preserveDanglingCloseParenthesis
package org.qirx.littlespec.fragments

import scala.concurrent.duration._
import scala.util.Random
import testUtils.assertion.NumericAssertEnhancements
import org.qirx.littlespec.Specification

object FragmentSpec extends Specification with NumericAssertEnhancements {


  "DefaultFragment" - {

    "instantiation should" - {
      def fakeOnFragmentCreated(f: Fragment => Unit): FragmentHandler.CancelSubscription = ???

      "not evaluate the code that is passed in" - {
        new DefaultFragment(null, ???, fakeOnFragmentCreated)
        success
      }

      "set the correct title" - {
        val title = Text("title-" + Random.nextInt)
        val fragment = new DefaultFragment(title, ???, fakeOnFragmentCreated)
        fragment.title is title
      }
    }

    "execution should" - {

      "return pending todo for unit" - {
        execute() is Pending(defaultTitle, "TODO")
      }

      "return pending todo for todo" - {
        val result = execute(todo)
        result is Pending(defaultTitle, "TODO")
      }

      "return pending for pending" - {
        val result = execute(pending("message"))
        result is Pending(defaultTitle, "message")
      }

      "return success for success" - {
        val result = execute(success)
        result isLike {
          case Success(title) => title is defaultTitle
        }
      }

      "return unexpected failure for an exception" - {
        val e = new RuntimeException("unexpected")
        val result = execute(throw e)
        result is UnexpectedFailure(defaultTitle, e)
      }

      "measure duration" - {
        val result1 = execute(success)
        val result2 = execute {
          Thread.sleep(101)
          success
        }

        result1 isLike {
          case s: Success => s.duration isLessThan 100.millis
        }
        result2 isLike {
          case s: Success => s.duration isMoreThan 100.millis
        }
      }

      "capture failures" - {
        val message = "custom failure"
        val result = execute(failure(message))
        result is Failure(defaultTitle, message, failureWithMessage(message))
      }

      "correctly handle nested fragments" - {
        val eventBus = new FragmentHandler

        def newFragment(title: String)(code: => Fragment.Body) = {
          val fragment = new DefaultFragment(Text(title), code, eventBus.onFragmentCreated)
          eventBus.fragmentCreated(fragment)
          fragment
        }

        val fragment =
          newFragment("level 1") {
            newFragment("level 2 - todo") {
              //todo
            }
            newFragment("level 2 - pending") {
              pending("pending")
            }
            newFragment("level 2 - nested") {
              newFragment("level 3 - failure") {
                failure("failure")
              }
              newFragment("level 3 - success") {
                success
              }
            }
          }

        val result = fragment.execute
        result is
          CompoundResult(Text("level 1"),
            Seq(
              Pending(Text("level 2 - todo"), "TODO"),
              Pending(Text("level 2 - pending"), "pending"),
              CompoundResult(Text("level 2 - nested"),
                Seq(
                  Failure(Text("level 3 - failure"), "failure", failureWithMessage("failure")),
                  Success(Text("level 3 - success"))(0.millis)
                )
              )
            )
          )
      }
    }
  }

  val defaultTitle = Text("title")

  def newFragment(code: => FragmentBody) =
    new DefaultFragment(defaultTitle, code, new FragmentHandler().onFragmentCreated)

  def execute(code: => FragmentBody) =
    newFragment(code).execute

  def failureWithMessage(message:String) =
    Fragment.Failure(message)

  implicit def numeric[T <: Duration]: Numeric[T] =
    new Numeric[T] {
      def fromInt(x: Int): T = ???
      def minus(x: T, y: T): T = ???
      def negate(x: T): T = ???
      def plus(x: T, y: T): T = ???
      def times(x: T, y: T): T = ???
      def toDouble(x: T): Double = ???
      def toFloat(x: T): Float = ???
      def toInt(x: T): Int = ???
      def toLong(x: T): Long = ???

      def compare(x: T, y: T): Int = x.compare(y)
    }
}