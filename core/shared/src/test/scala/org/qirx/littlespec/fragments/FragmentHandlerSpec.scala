package org.qirx.littlespec.fragments

import org.qirx.littlespec.Specification

object FragmentHandlerSpec extends Specification {

  private def newFragmentHandler = new FragmentHandler
  private val testFragment = new Fragment { def execute() = ??? }

  "FragmentHandler should" - {

    "accept subscriptions" - {
      newFragmentHandler.onFragmentCreated(identity)
      success
    }

    "handle events" - {
      newFragmentHandler.fragmentCreated(testFragment)
      success
    }

    "push events to subscribers" - {
      var event1: Option[Fragment] = None
      var event2: Option[Fragment] = None

      val eventBus = newFragmentHandler
      eventBus.onFragmentCreated(f => event1 = Some(f))
      eventBus.onFragmentCreated(f => event2 = Some(f))
      eventBus.fragmentCreated(testFragment)

      (event1) is Some(testFragment)
      (event2) is Some(testFragment)
    }

    "hand out a method to cancel the subscription" - {

      var event: Option[Fragment] = None

      val eventBus = newFragmentHandler
      val cancelSubscription = eventBus.onFragmentCreated(e => event = Some(e))
      cancelSubscription()
      eventBus.fragmentCreated(testFragment)

      (event) is None
    }
  }
}