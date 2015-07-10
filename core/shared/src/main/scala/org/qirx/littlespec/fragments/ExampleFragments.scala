package org.qirx.littlespec.fragments

import org.qirx.littlespec.macros.Location
import org.qirx.littlespec.io.Source

trait ExampleFragments { self: FragmentContainer =>

  def example[T](code: => T)(implicit asBody: T => Fragment.Body, location: Location): Fragment =
    createFragment(Source.codeAtLocation(location), code)

}