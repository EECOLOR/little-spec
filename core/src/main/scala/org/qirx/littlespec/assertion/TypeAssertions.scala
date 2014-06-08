package org.qirx.littlespec.assertion

import org.qirx.littlespec.fragments.Fragment
import scala.reflect.ClassTag

trait TypeAssertions { self:StaticAssertions =>

  def beAnInstanceOf[T: ClassTag] =
    new Assertion[Any] {
      def assert(obj: => Any): Either[String, Fragment.Body] = {
        val targetClass = implicitly[ClassTag[T]].runtimeClass
        val objClass = obj.getClass
        if (targetClass isAssignableFrom objClass) Right(success)
        else Left(objClass.getSimpleName + " is not an instance of " + targetClass.getSimpleName)
      }
    }
}