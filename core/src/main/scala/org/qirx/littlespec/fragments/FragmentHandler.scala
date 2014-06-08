package org.qirx.littlespec.fragments
import scala.collection.mutable.HashSet

class FragmentHandler {

  private val subscriptions = HashSet.empty[Fragment => Unit]

  def onFragmentCreated(handler: Fragment => Unit): FragmentHandler.CancelSubscription = {
    subscriptions += handler

    () => subscriptions -= handler
  }

  def fragmentCreated(fragment: Fragment): Unit =
    subscriptions.foreach(_ apply fragment)
}

object FragmentHandler {
  type CancelSubscription = () => Unit
}