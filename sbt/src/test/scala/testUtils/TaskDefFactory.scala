package testUtils

import sbt.testing.{ TaskDef => SbtTaskDef }
import sbt.testing.SubclassFingerprint
import sbt.testing.TestSelector
import sbt.testing.{TaskDef => SbtTaskDef}
import sbt.testing.{TaskDef => SbtTaskDef}

object TaskDefFactory {

  case class FingerPrint(isObject: Boolean) extends SubclassFingerprint {
    val isModule = isObject
    val requireNoArgConstructor = true
    val superclassName = "org.qirx.littlespec.Specification"
  }

  def create(qualifiedName: String, isObject: Boolean = false) =
    new SbtTaskDef(
        qualifiedName,
        FingerPrint(isObject),
        false,
        Array(new TestSelector(qualifiedName)))
}