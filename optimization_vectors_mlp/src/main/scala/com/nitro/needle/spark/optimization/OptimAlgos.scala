package com.nitro.needle.spark.optimization

import breeze.linalg.DenseVector
import com.nitro.needle.spark.optimization.Types.{GradFn, CostFn}
import com.nitro.needle.util.Unrolling
import fif.Data
import Data.ops._
import Types.{WeightUpdate, GradFn, CostFn}
import Unrolling.{roll, unroll, Unrollable}
import org.apache.log4j.Logger
import Sampling.sampleMiniBatch
import shapeless.Coproduct

/**
 * The optimizization algorithms (SGD, Adagrad, L-BFGS, etc.) go here.
 *
 * @author Marek Kolodziej
 */
object OptimAlgos {

  @transient val log = Logger.getLogger(OptimAlgos.getClass)

  // helper class to make the SGD and Adagrad code more DRY, since this is repetitive stuff
  private case class OptInfo[D[_]: Data](
                              private val data: D[VectorizedData],
                              private val miniBatchFraction: Double,
                              private val currSeed: Long,
                              private val history: OptHistory,
                              private val costFn: CostFn[D],
                              private val gradFn: GradFn[D]
                              ) {

    val weights = history.weights.last
    private val histLen = history.cost.size
    lazy val sample = sampleMiniBatch(data, miniBatchFraction, currSeed)
    lazy val sampleSize = sample.map(i => i.targets.rows).reduce(_ + _)
    lazy val newCost = costFn(sample, weights)
    lazy val gradients = gradFn(sample, weights)
    lazy val prevDeltaW = {
      val wts = history.weights
      val (wts1, wts2) = (wts(histLen - 1), wts(histLen - 2))
      val unrolled1 = unroll(wts1)
      val unrolled2 = unroll(wts2)
      val diff = unrolled1 :- unrolled2
      roll(elems = diff, underlyingDims = wts1)
    }
  }

  /* stochastic gradient descent
     see http://leon.bottou.org/publications/pdf/online-1998.pdf
   */
  def sgd[D[_]: Data] = WeightUpdate[D](
    f = (data: D[VectorizedData],
         history: OptHistory,
         gradFn: GradFn[D],
         costFn: CostFn[D],
         initAlpha: Double,
         momentum: Double,
         miniBatchFraction: Double,
         miniBatchIterNum: Int,
         seed: Long,
         annealing: Boolean) => {

      val opt = OptInfo(data, miniBatchFraction, seed + miniBatchIterNum, history, costFn, gradFn)
      val denom = if (annealing) (opt.sampleSize * miniBatchIterNum).toDouble else opt.sampleSize.toDouble
      val eta = initAlpha / denom
      val mom: DenseVector[Double] = unroll(opt.prevDeltaW) :* momentum
      val innerWts = unroll(opt.weights)
      val innerGrads = unroll(opt.gradients)
      val newWtsNoMom: DenseVector[Double] = innerWts :- (innerGrads :* eta)
      val gradWithMom = (innerGrads :* eta) :+ mom
      val newWtsWithMom = newWtsNoMom :+ mom

      OptHistory(
        cost = history.cost :+ opt.newCost,
        weights = {
          val last = history.weights.last
          val rolled: Seq[Unrollable] = roll(elems = newWtsWithMom, underlyingDims = last)
          history.weights :+ rolled
        },
        grads = {
          val last = history.grads.last
          val lastUnrolled = unroll(last)
          val combined = lastUnrolled :+ gradWithMom
          val rolled: Seq[Unrollable] = roll(elems = combined, underlyingDims = last)
          history.grads :+ rolled
        }
      )
    }
  )
}