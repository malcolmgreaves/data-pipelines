package com.nitro.needle.datahelpers

import breeze.linalg.DenseMatrix
import com.nitro.needle.spark.optimization.VectorizedData
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.io.BufferedSource
import scala.io.Source.fromFile
import scala.util.Random

import fif.ImplicitRddData._

object MnistTransforms extends Serializable {

  private[this] def grouped(examplesPerGroup: Int = 10): Vector[VectorizedData] = {
    val features = fromFile("src/main/resources/mnist/cs229/MNIST_features.csv").getLines.toVector
    val targets = fromFile("src/main/resources/mnist/cs229/MNIST_targets.csv").getLines.toVector
    val rand = new Random(42L)
    val tuples = rand.shuffle(features.zip(targets))
    tuples.grouped(examplesPerGroup).map(i => {

       val feat: Vector[Array[Double]] = i.map(_._1.split(",").map(_.toDouble))
       val targ = i.map(_._2.toInt)
       val featMat = new DenseMatrix(
         rows = feat(0).size,
         cols = feat.size,
         data = feat.flatten.toArray
       ).t

      val targMat = DenseMatrix.zeros[Double](feat.size, 10)
      for (i <- 0 until feat.size) {
        val y = if (targ(i) == 10) 0 else targ(i)
        targMat(i, y) = 1
      }
      VectorizedData(features = featMat, targets = targMat)
    }).toVector
  }

  def localData(examplesPerGroup: Int = 10): Traversable[VectorizedData] =
    grouped(examplesPerGroup)

  def rddData(examplesPerGroup: Int = 10, sc: SparkContext): RDD[VectorizedData] = {
      val gr = grouped(examplesPerGroup)
      sc.parallelize(gr)
    }

}
