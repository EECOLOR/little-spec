package org.qirx.littlespec.fragments

import org.qirx.littlespec.Specification

object FragmentContainerSpec extends Specification {

  "Specification should be able to execute fragments resulting in" - {

    def todoResult(title: String) = Pending(Text(title), "TODO")

    "no results if empty" - {
      val container = new Specification {}
      container.executeFragments() is Seq.empty
    }

    "result of one fragment if one is present" - {
      val container =
        new Specification {
          "test" - {}
        }
      container.executeFragments() is
        Seq(todoResult("test"))
    }

    "correct results of nested fragments" - {
      val container =
        new Specification {
          "test1" - {
            "test2" - {}
            "test3" - {}
          }
        }

      container.executeFragments() is
        Seq(CompoundResult(Text("test1"), Seq(todoResult("test2"), todoResult("test3"))))

      // and being able to repeat it
      container.executeFragments() is
        Seq(CompoundResult(Text("test1"), Seq(todoResult("test2"), todoResult("test3"))))
    }
  }
}
