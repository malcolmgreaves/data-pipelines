package snippets

import scala.language.reflectiveCalls

object IntermediateTraitsObjects extends App {
  //
  // [slide] traits and objects
  //

  // anonymous classes
  val annon = new {
    def scrooge = "Bah! Humbug!"
  }
  annon.scrooge

  // traits ~= Java interface (but a whole lot better, we'll see soon!)
  trait NoHolidayCheer {
    def scrooge: String
  }
  val noHoliday = new NoHolidayCheer {
    def scrooge = "Old Marley was as dead as a doornail."
  }
  noHoliday.scrooge

  // objects can extend traits
  object Singleton extends NoHolidayCheer {
    def scrooge =
      "If they would rather die, they had better do it, and decrease the surplus population!"
  }
  Singleton.scrooge

  // objects are first class too
  def printNoHolidayCheer(c: NoHolidayCheer) =
    println(s"${c.scrooge}")

  val single = Singleton
  printNoHolidayCheer(single)
  printNoHolidayCheer(Singleton)
  printNoHolidayCheer(noHoliday)

  //
  // [slide] syntactic sugar: "apply"
  //

  // A common Scala pattern:
  // Single-purpose objects/values have their method named apply.
  object Foobar {
    def apply(x: Int) =
      s"$x apples a day keeps the doctor away"
  }

  // the compilier re-writes this to "Foobar.apply(10)"
  val message = Foobar(10)
  println(message)

  // From the outside, pretty obvious what Square does: saures ints.
  trait Square {
    def apply(x: Int) = x * x
  }
  val baz = new Square {}
  println(baz(4))

  //
  // [slide] mix-in composition
  //

  trait HasMoney {
    def coins: Int
  }

  trait Social {
    def isLoney: Boolean
  }

  // We can inherient from multiple traits
  object Rich extends NoHolidayCheer with HasMoney with Social {

    // we can say "override" to make sure that the member is indeed overriding
    override def scrooge = "Bah! Humbug!"

    // the override keyword is optional, though
    def coins = 9001

    // can override a def with a val
    // but not the other way around
    override val isLoney = true
  }

  println(s"Rich's members")
  println(s"Rich says: ${Rich.scrooge}")
  println(s"Rich has ${Rich.coins} coins")
  // We can have any valid expression within ${} in string interpolation.
  println(s"""Is Rich lonely? ${if (Rich.isLoney) "yes" else "no"}""")

}
