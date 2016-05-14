package mlbigbook.ml

import fif.TravData
import org.scalatest.FunSuite

import scala.collection.Map

class CountingTest extends FunSuite {

  implicit val _ = TravData

  test("word count") {

    val words =
      "hello world how are you today hello hello I I say goodbye I say hello"
        .split(" ")
        .toTraversable

    val wcManual =
      words
        .foldLeft(Counting.empty[String, Int]) {
          case (m, word) => Counting.increment(m, word)
        }

    val correct =
      Map(
        "hello" -> 4l,
        "world" -> 1l,
        "how" -> 1l,
        "are" -> 1l,
        "you" -> 1l,
        "today" -> 1l,
        "I" -> 3l,
        "say" -> 2l,
        "goodbye" -> 1l
      )

    assert(wcManual == correct)
    val wcIncrement = Counting.count[String, Int, Traversable](words)

    assert(wcIncrement == correct)
  }

}

