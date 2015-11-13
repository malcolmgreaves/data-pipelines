package com.nitro.needle

import breeze.linalg.{argmax, DenseVector, DenseMatrix}
import com.nitro.needle.activations.Nonlinearities
import com.nitro.needle.implicits.SamplingImpl
import com.nitro.needle.neuralnet.common.TrainValTestSplit
import com.nitro.needle.neuralnet.mlp.{MlpSgd, MlpDataConf, MlpExperimentConf, MLP}
import com.nitro.needle.spark.optimization.VectorizedData
import com.nitro.needle.util.Unrolling
import Unrolling.{roll, unroll, Unrollable}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.scalatest.{BeforeAndAfterAll, FunSpec}
import shapeless.Coproduct
import Nonlinearities.Sigmoid

class NeedleSpec extends FunSpec with BeforeAndAfterAll {

  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  var sc: SparkContext = _

  override protected def beforeAll(): Unit = {

    val sparkConf =
      new SparkConf().
        setAppName("MLP XOR Unit Test").
        setMaster("local[1]")

    sc = new SparkContext(sparkConf)
  }

  override protected def afterAll(): Unit = {

    sc.stop()
  }

  describe("Rolling and unrolling") {

    val foo = DenseMatrix((1.0, 2.0, 3.0), (4.0, 5.0, 6.0))
    val bar = DenseMatrix((7.0, 8.0)).t
    val baz = DenseVector(9.0, 10.0, 11.0, 12.0)

    it("should work for both Densevectors and Densematrices"){

      val coproducts =
        Seq(Coproduct[Unrollable](foo), Coproduct[Unrollable](bar), Coproduct[Unrollable](baz))

      val unrolled = unroll(coproducts)
      val rolled = roll(unrolled, coproducts)

      assert(foo == rolled(0).select[DenseMatrix[Double]].get)
      assert(bar == rolled(1).select[DenseMatrix[Double]].get)
      assert(baz == rolled(2).select[DenseVector[Double]].get)
    }
  }

  describe("Multi-Layer Perceptron") {

    it("should learn the XOR function (non-linearly separable)") {

      implicit val localData = fif.RddData
      implicit val localSampling = SamplingImpl.RddSampling

      val randSeed = 42L

      val features =
        DenseVector(0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0).
          toDenseMatrix.
          reshape(2,4).
          t

      val targets =
        DenseVector(1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0).
          toDenseMatrix.
          reshape(2,4).
          t

      val vectorizedData =
        VectorizedData(features = features, targets = targets)

      val distData = sc.parallelize(Seq(vectorizedData))

      val dataConf = MlpDataConf(
        data = distData,
        trainValTestSplit = TrainValTestSplit(trainFrac = 1.0, validationFrac = 0.0),
        randSeed = randSeed
      )

      val experimentConf =
        MlpExperimentConf(
          optimizer = MlpSgd(
            learningRate = 0.9,
            annealing = None,
            miniBatchFraction = None,
            momentum = Some(0.9)
          ),
          l2RegLambda = 0.0,
          seed = randSeed,
          earlyStopWindow = Some(5),
          maxIter = 600
        )

      val mlp = MLP(
        layerSizes = Seq(3, 2),
        nonlinearities = Seq(Sigmoid, Sigmoid),
        dataConf = dataConf,
        experimentConf = experimentConf
      )

      mlp.train()

      val pred = mlp.predict(features = features)

      for (i <- 0 until features.rows) {

        println(
          s"""
             |input: ${features(i, ::).inner}
             |prediction: ${pred(i, ::).inner}
             |expected: ${targets(i, ::).inner}
             |predicted class: ${argmax(pred(i, ::).inner)}
             |expected class: ${argmax(targets(i, ::).inner)}
             |
           """.stripMargin)

        assert(argmax(pred(i, ::).inner) === argmax(targets(i, ::).t))

      }
    }
  }
}