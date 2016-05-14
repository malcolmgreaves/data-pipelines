package mlbigbook.text

trait Show[T] {
  def show(x: T): String
}

object ShowImplicits {

  implicit object ShowString extends Show[String] {
    override def show(x: String): String = x
  }

}