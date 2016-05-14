package snippets

object IntermediateHOFs extends App {

  //
  // [slide] Scala collections
  //

  Traversable("han", "shot", "first")
    .map { x => x.length }

  // if you * really * have to do a side effect....you can use foreach
  Traversable("han", "shot", "first")
    .foreach { println }

  Traversable("team", "coffee", "function")
    .filter { x => x.contains("fun") }

  // sorting and List

  List(3, 1, 4, 2)
    .sortBy { x => -x }

  // Unit == "hey I'm doing a side effect"
  // There's only one value for the Unit type. It's called...unit. Indicated
  // by ().
  val unit = ()

  // can pattern match on List because it is a recursively-defined datatype
  // In Scala, recursive functions need explicit return types
  def iterateAndPrint(l: List[Int]): Unit =
    l match {

      case head :: rest =>
        println(head)
        iterateAndPrint(rest)

      case nil =>
        println(">>> done! <<<")
    }

  iterateAndPrint(List(1, 2, 2, 3, 4, 5, 5, 6))

  // folding

  Traversable(1, 2, 3, 4)
    .foldLeft(0) {
      (sum, x) => sum + x
    }

  Traversable(1, 2, 3, 4)
    .foldRight(0) {
      (x, sum) => sum + x
    }

  Traversable(1, 2, 3, 4)
    .aggregate(0)(
      (sum, x) => sum + x,
      (sum1, sum2) => sum1 + sum2
    )

  Traversable("a", "b", "c")
    .foldLeft("") {
      (accumulate, s) => accumulate + s
    }

  Traversable("a", "b", "c")
    .foldRight("") {
      (s, accumulate) => accumulate + s
    }

  Traversable("a", "b", "c")
    .aggregate("")(
      (accumulate, s) => accumulate + s,
      (a1, a2) => a1 + a2
    )

  // the following 2 expressions are equivalent

  Traversable(1, 2, 3, 4)
    .reduce { (a: Int, b: Int) => a + b }

  // Scala has some syntactic support for inference using wildcards and
  // infix functions (e.g. + here is infix)
  Traversable(1, 2, 3, 4)
    .reduce(_ + _)

  // here's the classic Hadoop example

  val wordCount =
    """hello hello world how are you today I hope you are a nice world today goodbye world"""
      .split(" ")
      .map { word => (word, 1) }
      .foldLeft(Map.empty[String, Int]) {
        case (wc, (word, count)) =>
          wc.get(word) match {

            case Some(existingCount) =>
              (wc - word) + (word -> (existingCount + count))

            case None =>
              wc + (word -> count)
          }
      }
      .toList
      .sortBy {
        case (_, count) => -count
      }

  wordCount.foreach(println)

  // flattening

  val oneToX = (x: Int) => 1 to x

  IndexedSeq(1, 2, 3)
    .flatMap { oneToX }
  // compare to just using map
  IndexedSeq(125, 24, 33, 76, 98, 1)
    .map { oneToX }

  // the following 4 expressions (minus someIfEven) are equivalent

  val someIfEven =
    (x: Int) =>
      if (x % 2 == 0)
        Some(x)
      else
        None

  List(125, 24, 33, 76, 98, 1)
    .flatMap { x => someIfEven(x) }

  List(125, 24, 33, 76, 98, 1)
    .map { someIfEven }
    .flatten

  List(None, Some(24), None, Some(76), Some(98), None)
    .flatten

  List(24, 76, 98)

  //
  // [slide] fun with HOFs
  //

  type UserName = String
  type UUID = Long
  case class User(name: UserName, id: UUID)

  sealed trait Request
  case class FetchInformation(u: User) extends Request
  case object SystemStatus extends Request

  type Message = String
  case class PostToFeed(u: User, m: Message) extends Request

  case class Send(from: User, m: Message, to: User) extends Request

  def handle(r: Request) =
    r match {

      case FetchInformation(user) =>
        s"Fetching information for ${user}"

      case SystemStatus =>
        s"System OK. Current time: ${System.currentTimeMillis}ms"

      case PostToFeed(user, message) =>
        s"""Posting "$message" to feed for $user"""

      case Send(from, message, to) =>
        s"""Sending a message from user $from to user $to : "$message" """
    }

  val u1 = User("bobby", 125151245l)
  val u2 = User("sally", 666363l)
  Seq(
    SystemStatus,
    FetchInformation(u1),
    PostToFeed(u1, "omgz! I'm on the interwebzzzz"),
    PostToFeed(u1, "I am l33t"),
    Send(u2, "Please be quiet.", u1),
    SystemStatus

  ).map(handle)
    .foreach(println)

}
