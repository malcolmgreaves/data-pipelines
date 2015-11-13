package com.nitro.needle.spark.optimization

import com.nitro.needle.util.Unrolling
import fif.Data
import Unrolling.Unrollable

/**
 * Common types used throughout the optimization code base.
 *
 * @author Marek Kolodziej
 */
object Types {

  case class GradFn[D[_]: Data](f: (D[VectorizedData], Seq[Unrollable]) => Seq[Unrollable]) {
    def apply(data: D[VectorizedData], weights: Seq[Unrollable]) =
      f(data, weights)
  }

  case class CostFn[D[_]: Data](f: (D[VectorizedData], Seq[Unrollable]) => Double) {
    def apply(data: D[VectorizedData], weights: Seq[Unrollable]) =
      f(data, weights)
  }

  case class WeightUpdate[D[_]: Data](f: (D[VectorizedData], OptHistory, GradFn[D], CostFn[D],
    Double, Double, Double, Int, Long, Boolean) => OptHistory) {
    def apply(
               data: D[VectorizedData],
               history: OptHistory,
               gradFn: GradFn[D],
               costFn: CostFn[D],
               initAlpha: Double,
               momentum: Double,
               miniBatchFraction: Double,
               miniBatchIterNum: Int,
               seed: Long,
               annealing: Boolean
               ): OptHistory =
      f(
        data, history, gradFn, costFn, initAlpha,
        momentum, miniBatchFraction, miniBatchIterNum, seed, annealing
      )
  }

  trait WeightInit[T] {

    def apply(t: T, seed: Long): Seq[Unrollable]
  }

}
