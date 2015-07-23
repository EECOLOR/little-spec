package org.qirx.littlespec

import org.qirx.littlespec.assertion.StaticAssertions
import org.qirx.littlespec.assertion.BasicAssertEnhancements
import org.qirx.littlespec.assertion.FragmentBodyAssertEnhancement
import org.qirx.littlespec.assertion.TypeAssertions
import org.qirx.littlespec.assertion.ThrowingAssertions
import org.qirx.littlespec.fragments.ExampleFragments
import org.qirx.littlespec.fragments.FragmentContainer
import org.qirx.littlespec.fragments.Fragment

trait BaseSpecification
  extends FragmentContainer
  with ExampleFragments
  with StaticAssertions
  with BasicAssertEnhancements
  with FragmentBodyAssertEnhancement
  with ThrowingAssertions
  with TypeAssertions {

  type FragmentBody = Fragment.Body
}