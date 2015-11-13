package snippets

object AdvancedGenerics extends App {

  //
  // [slide] generics
  //

  // generics: unknown, specific types
  trait Show1[T] {
    def show(t: T): String
  }
  val s1 = new Show1[String] {
    def show(t: String) = t
  }
  println(s"""Show1: ${s1.show("Rick Sanchez")}""")

  // generics through abstract types
  trait Show2 {
    type T
    def show(t: T): String
  }
  val s2 = new Show2 {
    type T = String
    def show(t: T) = t
  }
  println(s"""Show2: ${s2.show("Morty Smith")}""")

}
