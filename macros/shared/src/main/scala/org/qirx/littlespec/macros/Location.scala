package org.qirx.littlespec.macros

import scala.reflect.macros.Context
import scala.language.experimental.macros

case class Location(filename: String, line: Int, column: Int, lines: Seq[String])

object Location {

  implicit def currentLocation: Location = macro currentLocationImpl

  def currentLocationImpl(c: Context): c.Expr[Location] = {
    import c.universe._
    val pos = c.macroApplication.pos
    val Location = c.mirror.staticModule(classOf[Location].getName)
    val path = pos.source.path
    val sourceLines = scala.io.Source.fromFile(path).getLines.toSeq
    val lines = q"Seq(..$sourceLines)"
    c.Expr(q"$Location($path, ${pos.line}, ${pos.column}, $lines)")
  }
}