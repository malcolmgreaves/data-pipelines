package mlbigbook.app

import fif.TravData
import mlbigbook.data.SimpleVec
import mlbigbook.ml.{ Euclidian, NearestNeighbors }
import mlbigbook.text.{ ShowImplicits, BagOfWords }
import scala.util.Random

object Experiment20NewsGroups extends App {

  implicit val _ = TravData

  import DataFor20Newsgroups._

  import ShowImplicits._

  val corpus = new TwentyNewsgroups

  if (corpus.documents.size == 0) {
    println(s"empty corpus in ${TwentyNewsgroups.dir20NG} -- exiting")
    sys.exit(1)
  }

  val (vectors, vectorizeable) = BagOfWords(Corpus.whitespaceTokenizer)(corpus.documents)

  val (trainingVectors, testingVectors) = {
    val shuffled =
      vectors
        .toIndexedSeq
        .map { x => (x, Random.nextDouble()) }
        .sortBy(_._2)
        .map(_._1)
    val splitIndex = Random.nextInt(shuffled.size - 2)
    (shuffled.slice(0, splitIndex), shuffled.slice(splitIndex, shuffled.size))
  }

  // make the Nearest Neighbors based ranker
  val nnRanker = {
    implicit val v = vectorizeable
    NearestNeighbors.ranker(Euclidian, trainingVectors.toTraversable.map(_._1))
  }

  def selectRandomly(data: Traversable[(String, SimpleVec)]): String =
    data.toIndexedSeq(Random.nextInt(data.size))._1

  val testingDoc =
    testingVectors.head._1
//    selectRandomly(testingVectors)

  val k = 5
  println(s"Using k= $k\n================\n\n\n\n")
  val topKToFirst = nnRanker.rank(testingDoc, k)

  val safeShowDoc =
    (doc: String) => doc.substring(0, math.min(3000, doc.length))

  println(s"Testing on document:\n================\n${safeShowDoc(testingDoc)}\n================\n================\n\n")

  topKToFirst
    .toSeq
    .zipWithIndex
    .foreach {
      case ((doc, score), index) =>
        val headingIsh = safeShowDoc(doc)
        println(s"#${index + 1} has score= $score\n================\n$headingIsh\n================\n\n")
    }
}

object DataFor20Newsgroups {

}