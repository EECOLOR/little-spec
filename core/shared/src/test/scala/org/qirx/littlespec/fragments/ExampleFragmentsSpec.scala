package org.qirx.littlespec.fragments

import org.qirx.littlespec.Specification
import testUtils.NoJSExportSpecification

object ExampleFragmentsSpec extends Specification {

  "Specification should provide a way to use code as an example" - {

    def todoResult(text: String) = Pending(Code(text), "TODO")

    def result(message: String) =
      CompoundResult(Text("test"), Seq(todoResult(message)))

    "empty" - {
      val c = new NoJSExportSpecification {
        "test" - example {}
      }
      c.executeFragments() is Seq(result(""))
    }

    "single line" - {
      val `this will result in a todo` = ()
      val expected = Seq(result("`this will result in a todo`"))

      "on a single line" - {
        val c = new NoJSExportSpecification {
          "test" - example { `this will result in a todo` }
        }
        c.executeFragments() is expected
      }

      "on multiple lines" - {
        val c = new NoJSExportSpecification {
          "test" - example {
            `this will result in a todo`
          }
        }
        c.executeFragments() is expected
      }
    }

    "multiline" - {
      def `this will` = ()
      val `result in a todo` = ()
      val c = new NoJSExportSpecification {
        "test" - example {
          `this will`
          `result in a todo`
        }
      }
      c.executeFragments() is
        Seq(result("""|`this will`
                      |`result in a todo`""".stripMargin))
    }

    "comments" - {
      val `result in a todo` = ()

      "single line" - {
        val c = new NoJSExportSpecification {
          "test" - example {
            // this will { } } {
            `result in a todo`
          }
        }

        c.executeFragments() is
          Seq(result("""|// this will { } } {
                        |`result in a todo`""".stripMargin))
      }

      "multiline on single line" - {
        val c = new NoJSExportSpecification {
          "test" - example { /* this will { } } { */ `result in a todo` }
        }
        c.executeFragments() is
          Seq(result("/* this will { } } { */ `result in a todo`"))
      }

      "multiline" - {

        val c = new NoJSExportSpecification {
          "test" - example {
            /*
             * this will { } } {
             */
            `result in a todo`
          }
        }
        c.executeFragments() is
          Seq(result("""|/*
                        | * this will { } } {
                        | */
                        |`result in a todo`""".stripMargin))
      }
    }

    "nested braces" - {
      val `result in a todo` = ()
      val c = new NoJSExportSpecification {
        "test" - example {
          {
            `result in a todo`
          }
        }
      }
      c.executeFragments() is
        Seq(result("""|{
                      |  `result in a todo`
                      |}""".stripMargin))
    }
  }

}