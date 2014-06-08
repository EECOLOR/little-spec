package documentation

import org.qirx.littlespec.fragments.Code
import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.Success

import testUtils.ExampleUtils

object ExampleFragments extends Specification with ExampleUtils {

  """|Sometimes you want to show how your library is used, the problem with
     |documenting code examples is that they 'rot'. In case you change your
     |library you will not be notified of any compile errors in your
     |documentation. On top of that, the code might not be doing what you
     |inteded. The `example` fragment helps you in these cases.""".stripMargin -
    new SpecificationExample {
       def specialMethod(x:Int) = 1 + x

       example {
         def result = specialMethod(1)
         result is 2
       }
    }.expecting {
      _ isLike { case Seq(Success(Code("def result = specialMethod(1)\nresult is 2"))) => success }
    }

}