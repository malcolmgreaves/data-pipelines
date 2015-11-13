package com.nitro.needle.neuralnet.mlp

import breeze.linalg.{max, DenseMatrix}
import com.nitro.needle.implicits.Sampling
import com.nitro.needle.spark.optimization.VectorizedData

import fif.Data
import fif.Data.ops._

case class OptResults(gradients: Seq[DenseMatrix[Double]], numExamples: Int)

case class MlpCostHistory(
                        trainingCost: Double,
                        validationCost: Option[Double]
                      )

case class MlpOptReturn(
                      newWeights: Seq[DenseMatrix[Double]]
                    )

case class MlpOptHistory(
                       trainingCost: Double,
                       validationCost: Option[Double],
                       optWeights: Seq[DenseMatrix[Double]],
                       optGradients: Seq[DenseMatrix[Double]]
                     )

case class MlpOptInput(
                     features: DenseMatrix[Double],
                     targets: DenseMatrix[Double],
                     initWeights: Seq[DenseMatrix[Double]]
                   )


sealed trait MlpOptimizer {

  def miniBatchFraction: Option[Double] = None
  def startingLearningRate: Option[Double] = None

  def optimize[D[_]: Data : Sampling](
                startingWeights: Seq[DenseMatrix[Double]],
                prevWeights: Option[Seq[DenseMatrix[Double]]] = None,
                data: D[VectorizedData],
                f: MlpOptInput => OptResults,
                maxIter: Int = 1,
                seed: Long = 42L,
                currIter: Int = 1
              ): MlpOptReturn
}

case class MlpSgd(
                learningRate: Double,
                annealing: Option[(Double, Int) => Double] = Some((initRate: Double, iterNum: Int) => (initRate / iterNum)),
                override val miniBatchFraction: Option[Double],
                momentum: Option[Double] = None,
                seed: Long = 42L

              ) extends MlpOptimizer {

  private[MlpSgd] case class CurrState(weights: Seq[DenseMatrix[Double]], prevDeltaW: Seq[DenseMatrix[Double]])

  @inline override def optimize[D[_]: Data : Sampling](
                         startingWeights: Seq[DenseMatrix[Double]],
                         prevWeights: Option[Seq[DenseMatrix[Double]]] = None,
                         data: D[VectorizedData],
                         f: MlpOptInput => OptResults,
                         maxIter: Int = 1,
                         seed: Long = 42L,
                         currIter: Int = 1
                       ): MlpOptReturn = {

    assert(maxIter >= 1)

    var currWts = startingWeights.map(i => i.copy)
    var prevDeltaW =
      prevWeights match {
        case Some(prevWts) => startingWeights.zip(prevWts).map(i => i._1 :- i._2)
        case None => startingWeights.map(i => DenseMatrix.zeros[Double](i.rows, i.cols))
      }

    val mom = momentum match {

      case Some(value) => value
      case None => 0.0D

    }

    for (iter <- 1 to maxIter) {

      val miniBatchSample: D[VectorizedData] = miniBatchFraction match {

        case None =>
          data

        case Some(frac) =>
          Sampling[D].sample(data)(withReplacement = false, fraction = frac, seed = seed + iter)
//          data.sample(withReplacement = false, fraction = frac, seed = seed + iter)
      }

      val finalEpochState = miniBatchSample.aggregate(CurrState(currWts, prevDeltaW))(

        seqOp = {
          case (acc: CurrState, elem: VectorizedData) =>

            val optIn = MlpOptInput(
              features = elem.features,
              targets = elem.targets,
              initWeights = currWts // acc.weights
            )

            val optResult = f(optIn)

            val currGrads = optResult.gradients
            val numEx = optResult.numExamples

            val annealedRate = annealing match {
              case None => learningRate
              case Some(schedule) => schedule(learningRate, max(1, maxIter - 1) * maxIter + iter)
            }

            val newWeights =
              acc.weights.zipWithIndex.map {
                case (weight, idx) =>
                  val dw = currGrads(idx) :* (-annealedRate / numEx)
                  weight :+ (dw :+ (acc.prevDeltaW(idx) :* mom))
              }

            val deltaW = newWeights.zip(acc.weights).map(i => i._2 :- i._1)

            CurrState(weights = newWeights, prevDeltaW = deltaW)
        },

        combOp = {

          case (acc1: CurrState, acc2: CurrState) =>

            val weights =
              acc1.weights.
                zip(acc2.weights).
                map(i => (i._1 :+ i._2) :* 0.5)

            val deltaW =
              acc1.prevDeltaW.
                zip(acc2.prevDeltaW).
                map(i => (i._1 :+ i._2) :* 0.5)

            CurrState(weights = weights, prevDeltaW = deltaW)
        }
      )

      currWts = finalEpochState.weights
      prevDeltaW = finalEpochState.prevDeltaW

      }

   MlpOptReturn(newWeights = currWts)

  }
}