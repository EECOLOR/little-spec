package testUtils.assertion

import scala.collection.Iterable
import org.qirx.littlespec.assertion.Assertion
import org.qirx.littlespec.fragments.Fragment
import scala.util.Try

trait CollectionAssertions {

  def contain(method: PartialFunction[Any, Fragment.Body]): Assertion[Iterable[_]] =
    new Assertion[Iterable[_]] {
      def assert(collection: => Iterable[_]): Either[String, Fragment.Body] = {

        val (lastFailure, result) =
          findLastFailureAndResult(collection.iterator)

        result
          .toRight("Could not find element that matches the given pattern")
          .left
          .map { message =>
            lastFailure
              .map("Could not find element, last failure: " + _)
              .getOrElse(message)
          }
      }

      private def findLastFailureAndResult(iterator: Iterator[_]) = {

        type Result = Option[Fragment.Body]
        type Failure = Option[String]

        def find(iterator: Iterator[_], failure: Failure): (Failure, Result) =
          if (iterator.hasNext) {
            val (newFailure, result) = execute(iterator.next)
            if (result.isDefined) None -> result
            else find(iterator, newFailure)
          } else failure -> None

        def execute(elem: Any): (Failure, Result) =
          if (method.isDefinedAt(elem))
            Try(method(elem))
              .map(result => None -> Some(result))
              .recover {
                case Fragment.Failure(message) =>
                  Some(message) -> None
              }.get
          else None -> None

        find(iterator, None)
      }
    }
}