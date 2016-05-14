package com.nitro.needle.spark.optimization

import com.nitro.needle.util.Unrolling
import fif.Data
import Data.ops._
import Unrolling.{Unrollable, zerosFromUnrollable}
import org.apache.log4j.Logger
import Types.{WeightInit, WeightUpdate, CostFn, GradFn}
import shapeless.Coproduct

/**
 * @author Marek Kolodziej
 */
object Optimizer {

  @transient val log = Logger.getLogger(Optimizer.getClass)

  def optimize[D[_]: Data](
                iter: Int,
                seed: Long = 42L,
                initAlpha: Double = 0.1,
                momentum: Double = 0.0,
                gradFn: GradFn[D],
                costFn: CostFn[D],
                updateFn: WeightUpdate[D],
                miniBatchFraction: Double,
                initWeights: Seq[Unrollable],
                data: D[VectorizedData],
                annealing: Boolean
                ): OptHistory = {

    val count = data.size
    val dataSize = data.headOption match {
      case Some(x) => x.features.cols
      case None => 0
    }
    val exampleCount = data.map(i => i.targets.rows).reduce(_ + _)
    val initCost = costFn(data, initWeights) / (miniBatchFraction * exampleCount)
    val initGrads = initWeights.map(i => zerosFromUnrollable(i))
    // we need 2 steps of history at initialization time for momentum to work correctly
    val initHistory = OptHistory(cost = Seq(initCost, initCost), weights = Seq(initWeights, initWeights), grads = Seq(initGrads, initGrads))

    (1 to iter).foldLeft(initHistory) {

      case (history, it) =>

        if (it == iter)
          history
        else
          updateFn(data, history, gradFn, costFn, initAlpha, momentum, miniBatchFraction, it, seed, annealing)
    }
  }
}
