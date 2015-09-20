package org.qirx.littlespec.sbt

class ArgumentExtractor(args:Array[String]) {

  def getArg(name: String) =
    args.sliding(2, 1).find(_.head == name).flatMap(_.lastOption)
}