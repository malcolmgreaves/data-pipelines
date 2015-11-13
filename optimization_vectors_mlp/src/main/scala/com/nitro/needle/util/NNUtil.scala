package com.nitro.needle.util

import breeze.linalg.{DenseMatrix, DenseVector}
import spire.syntax.cfor._

import scala.util.Random

object NNUtil {


  def indicatorFun(target: DenseVector[Int], numClasses: Int): DenseMatrix[Double] = {

    val kMat = DenseMatrix.eye[Double](numClasses)
    val yy = DenseMatrix.zeros[Double](numClasses, target.activeSize)

    cfor(0)(_ < target.activeSize, _ + 1) { i =>

      yy(::, i) := kMat(::, target(i))
      ()
    }
    yy.t
  }

 def glorotInitMat(currentSize: Int, prevSize: Int, seed: Long): DenseMatrix[Double] = {
   val eps = math.sqrt(6) / math.sqrt(currentSize + prevSize)
   val rand = new Random(seed)
   val arr = Array.fill(currentSize * prevSize)((2 * rand.nextDouble() - 1.0) * eps)
   new DenseMatrix[Double](rows = currentSize, cols = prevSize, data = arr)
 }

 def glorotInitVec(currentSize: Int, seed: Long): DenseVector[Double] = {
   val eps = math.sqrt(6) / math.sqrt(currentSize)
   val rand = new Random(seed)
   val arr = Array.fill(currentSize)((2 * rand.nextDouble() - 1.0) * eps)
   new DenseVector[Double](arr)
 }

 def accuracy(actual: DenseVector[Int], predicted: DenseVector[Int]) =
   (1.0 * (
     predicted.toArray.zip(actual.toArray).filter(i => i._1 == i._2).size)
   ) / predicted.activeSize

 def timeIt(block: => Any): Long = {
   System.gc()
   val start = System.currentTimeMillis
   block
   System.currentTimeMillis - start
 }

 /*
  def glorotInit(currentSize: Int, prevSize: Int, seed: Long): DenseMatrix[Double] = {
    val eps = math.sqrt(6) / math.sqrt(currentSize + prevSize)
    DenseMatrix.rand[Double](currentSize, prevSize, Uniform(-eps, eps))
  }

  def glorotInit(currentSize: Int, seed: Long): DenseVector[Double] = {
    val eps = math.sqrt(6) / math.sqrt(currentSize)
    DenseVector.rand[Double](currentSize, Uniform(-eps, eps))
  }
*/

}