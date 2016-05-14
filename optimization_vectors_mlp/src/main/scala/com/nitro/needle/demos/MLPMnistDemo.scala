package com.nitro.needle.demos

import breeze.linalg.argmax
import com.nitro.needle.activations.Nonlinearities
import com.nitro.needle.datahelpers.{MnistViz, MnistTransforms}
import com.nitro.needle.implicits.SamplingImpl
import com.nitro.needle.neuralnet.common.TrainValTestSplit
import com.nitro.needle.neuralnet.mlp.{MlpSgd, MlpDataConf, MlpExperimentConf, MLP}
import com.nitro.needle.plotting.MultiPlot
import com.nitro.needle.spark.optimization.VectorizedData
import com.nitro.needle.util.NNUtil
import fif.Data
import Nonlinearities.Sigmoid
import NNUtil.timeIt
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import Data.ops._
import SamplingImpl.{RddSampling, TravSampling}
import fif.Data.ops._
import fif.DataOps._
import fif.ImplicitRddData
import ImplicitRddData._
import fif.ImplicitRddData

object MLPMnistDemo extends App {

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  val seed = 123L

  val conf = new SparkConf().setMaster("local[4]").setAppName("MLP")
  val sc = new SparkContext(conf)

//  implicit val foo = RddSampling

  implicit val localData = fif.TravData
  val data = MnistTransforms.rddData(examplesPerGroup = 56, sc)

  println("Got data")

  val dataConf = MlpDataConf(
    data = data,
    // 56
//      MnistTransforms.localData(examplesPerGroup = 56),
    trainValTestSplit = TrainValTestSplit(trainFrac = 0.8, validationFrac = 0.1),
    randSeed = seed
  )

  val experimentConf = MlpExperimentConf(
    optimizer = MlpSgd(
      learningRate = 0.5, //0.05, // 0.05, // 0.01,
      annealing = Some((initRate: Double, iterNum: Int) => (initRate / iterNum)),
      miniBatchFraction = None, // Some(0.5),
      momentum = Some(0.5)
    ),
    l2RegLambda = 0.0,
    seed = seed,
    earlyStopWindow = Some(10),
    maxIter = 300
  )

//  implicit val rddSmpl = RddSampling

  val net = MLP(
    layerSizes = Seq(256, 10),
    nonlinearities = Seq(Sigmoid, Sigmoid),
    dataConf = dataConf,
    experimentConf
  )

  val trainingTime = timeIt(net.train())

  def accuracy[D[_]: Data](data: D[VectorizedData]): Double = {
    val (numCorrect, numTotal) =
      data.aggregate((0L, 0L))(

        seqOp = {

          case (acc, elem) =>
            var corr = 0
            val preds = net.predict(features = elem.features)
            val targets = elem.targets
            for (i <- 0 until preds.rows) {
              val predicted = argmax(preds(i, ::).inner)
              val actual = argmax(targets(i, ::).inner)
              if (predicted == actual) {
                corr += 1
              }
            }
            (acc._1 + corr, acc._2 + targets.rows)
        },

        combOp = {

          case (a, b) => (a._1 + b._1, a._2 + b._2)
        }

      )
    numCorrect.toDouble / numTotal
  }

  val trainAccuracy = accuracy(dataConf.trainData)
  val valAccuracy = accuracy(dataConf.valData)
  val testAccuracy = accuracy(dataConf.testData)

  println(s"MNIST training time: ${trainingTime / 1000} seconds")
  println(s"Training set accuracy: $trainAccuracy")
  println(s"Validation set accuracy: $valAccuracy")
  println(s"Test set accuracy: $testAccuracy")

  val trainCost = net.costHistory.map(_.trainingCost).toSeq
  // TODO: remove this ugly and unsafe call to get
  val valCost = net.costHistory.map(_.validationCost.get).toSeq

  val costPlot = MultiPlot(
    x = (1 to trainCost.size).map(_.toDouble).toSeq,
    y = Seq(trainCost, valCost),
    xAxis = "iteration",
    yAxis = "cost",
    title = "MNIST training and validation cost",
    legend = Seq("training cost", "validation cost"),
    port = 1234
  )

  costPlot.renderAndCloseOnAnyKey()

  val (mnistPlotSamples, predLabels): (Seq[Array[Double]], Seq[Int]) ={
    val features = dataConf.testData.first.features
    println(s"Test features: ${features.rows} rows x ${features.cols} columns")
    val numToShow = math.min(features.rows, 56)
    val images = for (i <- 0 until numToShow) yield features(i, ::).inner.toArray
    val predictions = {
      val temp = net.predict(features)
      for (i <- 0 until numToShow) yield argmax(temp(i, ::).inner)
    }
    (images, predictions)
  }

  val viz = new MnistViz(
    figTitle = "Handwritten digit predictions",
    features = mnistPlotSamples,
    targets = predLabels
  )

  sc.stop()

}
