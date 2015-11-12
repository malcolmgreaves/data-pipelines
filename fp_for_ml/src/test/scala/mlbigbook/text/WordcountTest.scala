package mlbigbook.text

import fif.Data
import Data.ops._
import fif.TravData
import mlbigbook.ml.Counting
import org.scalatest.FunSuite

class WordcountTest extends FunSuite {

  import WordcountTest._

  implicit val evidence = TravData

  test("[seq] wordcount sentence") {
    assertCountsL(actualCounts(idFox), Counting.count[String, Long, Traversable](sentFox))
    assertCountsL(actualCounts(idSanta), Counting.count[String, Long, Traversable](sentSanta))
  }

  test("[seq] wordcount document") {
    assertCountsL(actualCounts(idFox), Counting.count[String, Long, Traversable](docFox.flatten))
    assertCountsL(actualCounts(idSanta), Counting.count[String, Long, Traversable](docSanta.flatten))
    assertCountsL(actualCounts(idBoth), Counting.count[String, Long, Traversable](docBoth.flatten))
  }

  test("[seq] wordcount corpus") {
    assertCountsL(actualCounts(idCorpus), Counting.count[String, Long, Traversable](corpus.flatMap(identity).flatten))
  }

  def assertCountsL(actual: Map[String, Long], counted: Map[String, Long]) = {
    counted.foreach({
      case (word, count) => assert(
        actual(word) == count,
        s"""\"$word\" is unexpected (has count $count)"""
      )
    })

    assert(counted.size == actual.size, s"${counted.size} found, expecting ${actual.size} entries")

    val actualSum = actual.map(_._2).foldLeft(0L)(_ + _)
    val countedSum = counted.map(_._2).foldLeft(0L)(_ + _)
    assert(actualSum == countedSum, s"$countedSum total counts, expecting $actualSum total counts")
  }
}

object WordcountTest {

  trait ID {
    def id(): Int
  }

  val sentFox = Traversable("the", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog")
  val docFox = Seq(sentFox)
  val idFox = 0

  val sentSanta = Traversable("santa", "claus", "is", "coming", "to", "town")
  val docSanta = Seq(sentSanta)
  val idSanta = 1

  val docBoth = Seq(sentFox, sentSanta)
  val idBoth = 2

  val corpus = Seq(docFox, docSanta, docBoth)
  val idCorpus = 3

  val actualCounts: Map[Int, Map[String, Long]] = Map(
    idFox -> Map(
      "the" -> 2L,
      "quick" -> 1L,
      "brown" -> 1L,
      "fox" -> 1L,
      "jumped" -> 1L,
      "over" -> 1L,
      "lazy" -> 1L,
      "dog" -> 1L
    ),
    idSanta -> Map(
      "santa" -> 1L,
      "claus" -> 1L,
      "is" -> 1L,
      "coming" -> 1L,
      "to" -> 1L,
      "town" -> 1L
    ),
    idBoth -> Map(
      "the" -> 2L,
      "quick" -> 1L,
      "brown" -> 1L,
      "fox" -> 1L,
      "jumped" -> 1L,
      "over" -> 1L,
      "lazy" -> 1L,
      "dog" -> 1L,
      "santa" -> 1L,
      "claus" -> 1L,
      "is" -> 1L,
      "coming" -> 1L,
      "to" -> 1L,
      "town" -> 1L
    ),
    idCorpus -> Map(
      "the" -> 4L,
      "quick" -> 2L,
      "brown" -> 2L,
      "fox" -> 2L,
      "jumped" -> 2L,
      "over" -> 2L,
      "lazy" -> 2L,
      "dog" -> 2L,
      "santa" -> 2L,
      "claus" -> 2L,
      "is" -> 2L,
      "coming" -> 2L,
      "to" -> 2L,
      "town" -> 2L
    )
  )

}