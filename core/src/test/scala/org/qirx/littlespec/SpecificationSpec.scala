package org.qirx.littlespec

import org.qirx.littlespec.assertion.StaticAssertions
import org.qirx.littlespec.assertion.BasicAssertEnhancements
import org.qirx.littlespec.assertion.TypeAssertions
import org.qirx.littlespec.assertion.FragmentBodyAssertEnhancement
import org.qirx.littlespec.assertion.ThrowingAssertions
import org.qirx.littlespec.fragments.ExampleFragments
import org.qirx.littlespec.fragments.FragmentContainer
import org.qirx.littlespec.fragments.Fragment

object SpecificationSpec extends Specification {

  "Specification should" - {

    "be of the correct type" - {
      val specification = new Specification {}
      type Fragments = FragmentContainer with ExampleFragments
      type DefaultAssertions = StaticAssertions with ThrowingAssertions with TypeAssertions
      type DefaultAssertEnhancements = BasicAssertEnhancements with FragmentBodyAssertEnhancement
      type CorrectType = Fragments with DefaultAssertions with DefaultAssertEnhancements
      specification must beAnInstanceOf[CorrectType]
    }

    "have a type alias for body" - {
      val specification = new Specification {}
      val x: specification.FragmentBody = todo: Fragment.Body
      success
    }
  }
}