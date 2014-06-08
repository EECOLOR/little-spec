package org.qirx.littlespec.macros

import scala.reflect.macros.Context
import scala.language.experimental.macros

case class Location(filename: String, line: Int, column: Int)

object Location {

  implicit def currentLocation:Location = macro currentLocationImpl

  def currentLocationImpl(c: Context): c.Expr[Location] = {
    import c.universe._
    val pos = c.macroApplication.pos
    val Location = c.mirror.staticModule(classOf[Location].getName)
    c.Expr(Apply(Ident(Location), List(Literal(Constant(pos.source.path)), Literal(Constant(pos.line)), Literal(Constant(pos.column)))))
  }
}
