package testUtils

import org.qirx.littlespec.fragments.Result
import org.qirx.littlespec.fragments.FragmentContainer
import org.qirx.littlespec.fragments.Fragment
import org.qirx.littlespec.io.Source
import org.qirx.littlespec.macros.Location
import org.qirx.littlespec.UnannotatedSpecification
import org.qirx.littlespec.Specification

trait ExampleUtils { self: Specification =>

  class SpecificationExample(implicit location: Location) extends UnannotatedSpecification {
    def expecting(result: Seq[Result] => Fragment.Body) = {
      self.createFragment(Source.codeAtLocation(location), result(this.executeFragments()))
    }
  }

  class Example(implicit location: Location) { self =>
    def expecting(result: self.type => Fragment.Body) =
      createFragment(Source.codeAtLocation(location), result(self))
  }
}
