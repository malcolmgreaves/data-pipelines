package snippets

// This code is meant to be inspected and executed in the REPL in chunks.
object IntroVarsValsBuiltIns {

  //
  // [slide] values, vars, type inference
  //

  // values: non-reassignable
  val i: Int = 10

  /*

  i = 20 // fails

  */

  // vars: reassignable (be careful!)
  var x = "hello"
  x = "world"

  // type inference
  val fooD = 10.0 // Double
  val fooF = 10.0f // Float
  val fooI = 10 // Int
  val fooL = 10l // Long

  // delayed evaluation
  lazy val bar = println("bar bar bar!")
  println(bar)

  lazy val dangerZone = if(true) throw new RuntimeException("Oh noes!")

  /*

  // using "dangerZone" will cause the right-hand-side to be evaluated,
  // which means we'll throw a RuntimeException
  val _ = dangerZone
  
  // lazy does NOT work with var for good reason!
  lazy var noLazyVars = 10 // fails
  
  */

  //
  // [slide] tuples
  //

  ("a", 10)
  (("a", "b"), (20, 30))

  // select element k with "._k"
  (1, "hello", 10.0)._1 == 1
  (1, "hello", 10.0)._2 == "hello"
  (1, "hello", 10.0)._3 == 10.0

  //
  // [slide] strings, printing, built-ins
  //

  // string interpolation
  val toPrint = s"$x $fooD ${-1 * 9}"
  println(toPrint)

  // literal strings
  println(s"""Wow, I can use "quotes" in a literal string!""")

  /*
  // copy and paste following into REPL

  // conversions
  "10".toInt
  "10".toDouble
  40.toString
  "hello world".hashCode

  // mathematical expressions
  5 * 10
  // Java rules for numerical conversions: higher precision
  40.0 + 10
  49l + 1
  49l + 1 == 40 + 10

  */

  println(
    s"""
       |// conversions
       |"10".toInt
       |==>
       |  ${"10".toInt}
       |"10".toDouble
       |==>
       |  ${"10".toDouble}
       |40.toString
       |==>
       |  ${40.toString}
       |"hello world".hashCode
       |==>
       |  ${"hello world".hashCode}
       |
       |// mathematical expressions
       |5 * 10
       |==>
       |  ${5 * 10}
       |// Java rules for numerical conversions: higher precision
       |40.0 + 10
       |==>
       |  ${40.0 + 10}
       |49l + 1
       |==>
       |  ${49l + 1}
       |49l + 1 == 40 + 10
       |==>
       |  ${49l + 1 == 40 + 10}
     """.stripMargin
  )

}