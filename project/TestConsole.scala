import scala.scalajs.tools.env.JSConsole

class TestConsole extends JSConsole {
  def log(msg: Any): Unit = {
    println("test: " + msg)
    Console.println("test: " + msg)
    Console.println(msg)
  }
}