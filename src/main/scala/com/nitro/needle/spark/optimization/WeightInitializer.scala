package com.nitro.needle.spark.optimization

import breeze.linalg.DenseVector
import com.nitro.needle.spark.optimization.Types.WeightInit
import Types.WeightInit
import com.nitro.needle.util.Unrolling
import Unrolling.Unrollable
import shapeless.Coproduct

import scala.util.Random

/**
 * @author Marek Kolodziej
 */
object WeightInitializer {

  case object GaussianLinRegInit extends WeightInit[Int] {

    override def apply(numEl: Int, seed: Long = 42L): Seq[Unrollable] = {
      val rand = new Random(seed)
      val arr = Array.fill(numEl)(rand.nextGaussian())
      val dv = new DenseVector[Double](arr)
      Seq(Coproduct[Unrollable](dv))
    }
  }
}
