package snippets

// This code is meant to be inspected and executed in the REPL in chunks.
object IntroFunctionsMethods {

  //
  // [slide] functions
  //

  // anonymous functions
  (name: String, age: Int) =>
    s"$name is $age years old" 

  // functions are first-class!
  val firstClass: (String, Int) => String = 
    (name, age) => s"$name  is $age years old"

  firstClass("john", 9001)

  val printJohn =
    (f: (String, Int) => String) => f("john", 9001)

  printJohn(firstClass)
 
  // just like scope
  // the last line of a method or function is the value
  (a: Int, b: Int, c: Int) => { 
    val temp = a + b
    temp + c 
  }
 
  //
  // [slide] methods
  //

  // methods: named functions
  def method(name: String, age: Int) = 
    s"$name  is $age years old"

  // can either infer or explicitly state return type
  def methodExplicit(name: String, age: Int): String = 
    s"$name is $age years old"

  // methods have named arguments
  method(name = "Diane", age = 13)
  // order doesn't matter
  method(age = 13, name = "Diane")

  // partial application: from method to function !
  val m = method _
  // m has type (String, Int) => String
  m("john", 9001)
  
  // currying
  def currying(name: String)(age: Int)(birth: String) = 
    s"$name  is $age  years old and was born in $birth "
  // currying("helena")(5)("Mozambique")
  println(
    s"""
       |currying("helena")(5)("Mozambique")
       |==>
       |  ${currying("helena")(5)("Mozambique")}""".stripMargin
  )

  // fixing arguments
  val john = currying("john")_
  val withAge = john(44)
  withAge("Scotland")

  // a bit more involved, but can do w/ any arguments
  val inScotland =
    (name: String, age: Int) =>
      currying(name)(age)("Scotland")

  inScotland("jack", 1)

}