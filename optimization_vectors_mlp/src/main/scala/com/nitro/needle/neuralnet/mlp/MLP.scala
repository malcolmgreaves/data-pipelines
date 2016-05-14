package com.nitro.needle.neuralnet.mlp

import breeze.linalg._
import com.nitro.needle.activations.Nonlinearities
import com.nitro.needle.activations.Nonlinearities.Nonlinearity
import com.nitro.needle.implicits.Sampling
import com.nitro.needle.spark.optimization.VectorizedData
import com.nitro.needle.util.NNUtil
import com.nitro.needle.util.NNUtil.glorotInitMat
import fif.Data
import fif.Data.ops._
import spire.syntax.cfor._

import scala.collection.mutable.ArrayBuffer
import scala.math._

case class Samples(features: DenseMatrix[Double], targets: DenseMatrix[Double])

case class MlpForwardPassResults(
                                  as: Seq[DenseMatrix[Double]],
                                  zs: Seq[DenseMatrix[Double]],
                                  h: DenseMatrix[Double]
                                )

case class MlpBackwardPassResults(
                                   grads: Seq[DenseMatrix[Double]]
                                 )

case class MlpTrainValidationSplit(
                                    trainFeatures: DenseMatrix[Double],
                                    trainTarget: DenseVector[Int],
                                    valFeatures: DenseMatrix[Double],
                                    valTarget: DenseVector[Int]
                                  )


case class MLP[D[_] : Data : Sampling](
                 layerSizes: Seq[Int],
                 nonlinearities: Seq[Nonlinearity],
                 dataConf: MlpDataConf[D],
                 experimentConf: MlpExperimentConf
               ) {

  assert(
    layerSizes.size == nonlinearities.size,
    s"Nonlinearities has ${nonlinearities.size} elements but layers have ${layerSizes.size} elements")

  private[this] var weights: Seq[DenseMatrix[Double]] =
    experimentConf.startingWeights match {
      case Some(wts) => wts
      case None => {
        val seed = experimentConf.seed
        // TODO: get rid of the unsafe call to get
        val numFeatures = dataConf.trainData.headOption.get.features.cols
        for (i <- 0 until layerSizes.size)
          yield {
            if (i == 0)
              glorotInitMat(layerSizes(i), numFeatures + 1, seed + i)
            else
              glorotInitMat(layerSizes(i), layerSizes(i - 1) + 1, seed + i)
          }
      }
    }

  private[this] def copyWeights(wts: Seq[DenseMatrix[Double]]): Seq[DenseMatrix[Double]] = wts.map(_.copy)

  var costHistory: ArrayBuffer[MlpCostHistory] = new ArrayBuffer[MlpCostHistory]()

  def train(): this.type = {

    // TODO: prevent training a second time (need a "set-once" object to keep track of this)

    costHistory.clear()
    var weightHistory: ArrayBuffer[Seq[DenseMatrix[Double]]] = new ArrayBuffer[Seq[DenseMatrix[Double]]]()

    // TODO: get rid of the unsafe call to get
    val numClasses = dataConf.trainData.headOption.get.targets.cols

    val maxEpochs = experimentConf.maxIter
    val earlyStopWindow = experimentConf.earlyStopWindow

    var earlyStop = false

    println()

    for {

      i <- 1 to maxEpochs
      if !earlyStop
    } {

      println(s"Mini-batch $i")

      val wtsToGrads =
        (input: MlpOptInput) => {

          val fwd = forward(
            features = input.features
          )
          val bwd = backward(
            fwResults = fwd,
            features = input.features,
            target = input.targets
          )

          OptResults(gradients = bwd.grads, numExamples = input.features.rows)

        }

      val opt = experimentConf.optimizer.optimize(
        startingWeights = weights,
        prevWeights = weightHistory.lastOption,
        data = dataConf.trainData,
        f = wtsToGrads,
        maxIter = 1,
        seed = i,
        currIter = i
      )

      weights = opt.newWeights

      def cost(data: D[VectorizedData]): Double = {
        data.aggregate(0.0)(

          seqOp = {

            case (partialCost, datum) =>

              val pred = forward(features = datum.features, weights = weights)
              classificationCost(indicators = datum.targets, prediction = pred.h)
          },

          combOp = {

            case (a, b) => (a + b) / 2.0

          }

        )
      }

      val trainCost = cost(dataConf.trainData)
      val valCost = cost(dataConf.valData)

      println(s"Training cost: $trainCost, Validation cost = $valCost")

      earlyStopWindow match {

        case None =>
        case Some(numSteps) => {

          if (i <= numSteps) {

            costHistory += MlpCostHistory(trainingCost = trainCost, validationCost = Some(valCost))
            weightHistory += opt.newWeights

          } else {

            costHistory += MlpCostHistory(trainingCost = trainCost, validationCost = Some(valCost))

            val lastN = costHistory.
              drop(costHistory.size - numSteps).
              // TODO: get rid of the unsafe .get() call
              map(_.validationCost.get)

            val monotIncr = lastN.sliding(2).forall(i => i(0) < i(1))

            if (monotIncr) {
              println(s"\nEarly stop after $i epochs due to a monotonic increase of validation cost over $numSteps epochs (user conf.)\n")
              earlyStop = true
              weights = weightHistory.head
            }

            weightHistory = weightHistory.tail :+ weights
          }

        }
      }

    }
    println()

    this
  }

  def predict(features: DenseMatrix[Double]): DenseMatrix[Double] = {

    forward(
      features = features
    ).h
  }

  def predictClass(features: DenseMatrix[Double]): DenseVector[Int] = {

    argmax(predict(features), Axis._1)
  }

  def softmax(mat: DenseMatrix[Double]): DenseMatrix[Double] = {

    val denom = mat.
      map(d => exp(d)).
      toDenseVector.
      toArray.
      sum

    mat.map(d => exp(d) / denom)
  }

  protected def forward(
                                  features: DenseMatrix[Double],
                                  weights: Seq[DenseMatrix[Double]] = this.weights
                                  ): MlpForwardPassResults = {

    val zs = new ArrayBuffer[DenseMatrix[Double]]()
    val as = new ArrayBuffer[DenseMatrix[Double]]()

    val inputs =
      DenseMatrix.horzcat(
        DenseMatrix.ones[Double](features.rows, 1),
        features
      )

    as += inputs

    cfor(0)(_ < weights.size, _ + 1) { i =>

      zs += {
        val mult: DenseMatrix[Double] = weights(i) * as.last.t
        mult.t
      }
      as += {
        if (i == weights.size - 1) {
          nonlinearities(i).sigma(zs.last)
        } else {
          DenseMatrix.horzcat(
            DenseMatrix.ones[Double](zs.last.rows, 1),
            nonlinearities(i).sigma(zs.last)
          )
        }
      }
      ()
    }

    MlpForwardPassResults(as = as.toIndexedSeq, zs = zs.toIndexedSeq, h = as.last)

  }

  def noBias(weights: DenseMatrix[Double]): DenseMatrix[Double] =
    weights(::, 1 until weights.cols)

  def noBias(weights: Seq[DenseMatrix[Double]]): Seq[DenseMatrix[Double]] =
    weights.map(i => i(::, 1 until i.cols))

  def classificationCost(indicators: DenseMatrix[Double], prediction: DenseMatrix[Double]): Double = {
      val numEl = indicators.rows
      var numCorrect = 0
      cfor(0)(_ < numEl, _ + 1) { i =>
        if (argmax(indicators(i, ::).inner) == argmax(prediction(i, ::).inner)) {
          numCorrect += 1
        }
      }

//    -sum(indicators :* breeze.numerics.log(prediction))
    1.0 - numCorrect.toDouble / numEl
  }

  protected def backward(
                                   fwResults: MlpForwardPassResults,
                                   features: DenseMatrix[Double],
                                   target: DenseMatrix[Double],
                                   weights: Seq[DenseMatrix[Double]] = this.weights
                                   ): MlpBackwardPassResults = {


    val lambda = experimentConf.l2RegLambda
    val derivatives = nonlinearities.map(i => (j: DenseMatrix[Double]) => i.sigmaPrime(j))
    val deltaL = fwResults.h :- target
    val as = fwResults.as
    val zs = fwResults.zs
    val noB = noBias(weights)

    val ds = new ArrayBuffer[DenseMatrix[Double]]()
    ds += deltaL

    val invRange =  1 until zs.size// (weights.size - 1) to 1 by -1
    val s = invRange.toArray

    cfor(weights.size - 1)(_ >= 1, _ - 1) { i =>

      val mult: DenseMatrix[Double] = ds.last * noB(i)
      val grad = derivatives(i)(zs(i - 1))
      ds += mult :* grad
      ()
    }

    val deltas = new ArrayBuffer[DenseMatrix[Double]]()

    val numEl = target.rows.toDouble

    cfor(ds.size - 1)(_ >= 0, _ - 1) { i =>

      val current = (ds.reverse(i).t * as(i))
      current(::, 1 until current.cols) :+= noB(i) * lambda
      deltas += current
      ()
    }

    MlpBackwardPassResults(grads = deltas.reverse.toIndexedSeq)
  }

}

