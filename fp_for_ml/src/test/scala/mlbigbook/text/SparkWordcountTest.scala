package mlbigbook.text

import fif.RddData
import mlbigbook.LocalSparkContext
import mlbigbook.ml.Counting
import org.apache.spark.rdd.RDD
import org.scalatest.FunSuite

class SparkWordcountTest extends FunSuite with LocalSparkContext {

  import WordcountTest._

  implicit val evidence = RddData

  test("[RDD] wordcount corpus") {
    val corpusRDD = sc.parallelize(corpus)
    val corpusCounts = Counting.count[String, Long, RDD](
      corpusRDD.flatMap(_.flatMap(identity))
    )
    assertCountsL(actualCounts(idCorpus), corpusCounts)
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