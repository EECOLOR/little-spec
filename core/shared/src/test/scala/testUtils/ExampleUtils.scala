package testUtils

import org.qirx.littlespec.fragments.Result
import org.qirx.littlespec.Specification
import org.qirx.littlespec.io.Source
import org.qirx.littlespec.macros.Location

trait ExampleUtils { self:Specification =>

  class SpecificationExample(implicit location: Location) extends NoJSExportSpecification {
    def expecting(result: Seq[Result] => FragmentBody) = {
      self.createFragment(Source.codeAtLocation(location), result(this.executeFragments()))
    }
  }

  class Example(implicit location: Location) { self =>
    def expecting(result: self.type => FragmentBody) =
      createFragment(Source.codeAtLocation(location), result(self))
  }
}