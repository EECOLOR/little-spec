package org.qirx.littlespec.fragments

import scala.collection.mutable.ListBuffer

trait FragmentContainer {

  private val fragments = ListBuffer.empty[Fragment]
  private var results: Seq[Result] = Seq.empty
  private var executeFragmentsCalled: Boolean = false

  def executeFragments() = {
    if (!executeFragmentsCalled) {
      executeFragmentsCalled = true
      results = fragments.map(_.execute())
    }
    results
  }

  private val fragmentHandler = new FragmentHandler

  protected def createFragment(title: Title, body: => Fragment.Body): Fragment = {
    val fragment = new DefaultFragment(title, body, fragmentHandler.onFragmentCreated)
    if (!executeFragmentsCalled) fragments += fragment
    fragmentHandler.fragmentCreated(fragment)
    fragment
  }

  protected def createFragment(title: String, body: => Fragment.Body): Fragment =
    createFragment(Text(title), body)

  protected implicit class FragmentConstructor(title: String) {

    def -[T](code: => T)(implicit asBody: T => Fragment.Body): Fragment =
      createFragment(title, code)
  }
}