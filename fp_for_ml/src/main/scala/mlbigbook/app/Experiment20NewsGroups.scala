package mlbigbook.app

import java.io.{ FileReader, BufferedReader, File }

import fif.TravData
import mlbigbook.data.SimpleVec
import mlbigbook.ml.{ Euclidian, NearestNeighbors }
import mlbigbook.text.{ ShowImplicits, BagOfWords }
import org.apache.spark.SparkContext

import scala.io.Source
import scala.util.Random

object Experiment20NewsGroups extends App {

  def load(fi: File): String = {
    val br = new BufferedReader(new FileReader(fi))
    val sb = new StringBuilder()
    var line: String = br.readLine()
    while (line != null) {
      sb
        .append(line)
        .append("\n")
      line = br.readLine()
    }
    sb.toString()
  }

  def whitespaceTokenizer(s: String): IndexedSeq[String] =
    s.split("\\\\s+").toIndexedSeq

  implicit val _ = TravData

  import DataFor20Newsgroups._

  def loadDir(d: File): Traversable[String] =
    Option(d.listFiles())
      .map { files =>
        files
          .map(load)
          .toTraversable
      }
      .getOrElse(Traversable.empty[String])

  val byNewsgroup =
    newsgroups
      .map { ng =>
        println(s"Loading $ng")
        (ng, loadDir(fileAt(dir20NG)(ng)))
      }

  val corpus: Traversable[String] = byNewsgroup.flatMap(_._2).toTraversable

  println(s"${corpus.size} documents across ${byNewsgroup.size} newsgroups")

  import ShowImplicits._
  val (vectors, vectorizeable) = BagOfWords(whitespaceTokenizer)(corpus)

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
    //    testingVectors.head._1 // is alt.atheism
    selectRandomly(testingVectors)

  val k = 10
  println(s"Using k= $k\n================\n\n\n\n")
  val topKToFirst = nnRanker.rank(testingDoc, k)

  val safeShowDoc =
    (doc: String) => doc.substring(0, math.min(1000, doc.length))

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

  def fileAt(parent: File)(parts: String*) =
    parts.foldLeft(parent)((f, p) => new File(f, p))

  val dataDir = fileAt(new File("."))("data")
  val dir20NG = fileAt(dataDir)("20_newsgroups")

  val newsgroups = IndexedSeq(
    "alt.atheism",
    "comp.graphics",
    "comp.os.ms-windows.misc",
    "comp.sys.ibm.pc.hardware",
    "comp.sys.mac.hardware",
    "comp.windows.x",
    "misc.forsale",
    "rec.autos",
    "rec.motorcycles",
    "rec.sport.baseball",
    "rec.sport.hockey",
    "sci.crypt",
    "sci.electronics",
    "sci.med",
    "sci.space",
    "soc.religion.christian",
    "talk.politics.guns",
    "talk.politics.mideast",
    "talk.politics.misc",
    "talk.religion.misc"
  )

}