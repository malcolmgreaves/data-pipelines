package com.nitro.needle.neuralnet.mlp

import com.nitro.needle.neuralnet.common.TrainValTestSplit
import com.nitro.needle.spark.optimization.VectorizedData
import fif.Data
import Data.ops._
import scala.util.Random

/**
 *
 * @param data
 * @param trainValTestSplit
 * @param randSeed
 *
 * @author Marek Kolodziej
 */
case class MlpDataConf[D[_] : Data](
                        data: D[VectorizedData],
                        trainValTestSplit: TrainValTestSplit,
                        randSeed: Long = System.nanoTime
                        ) {

   private[this] lazy val count = data.size
   private[this] lazy val rand = new Random(randSeed)
   private[this] lazy val dataWithRand =
      data.map(i => (i, rand.nextDouble()))
    lazy val trainData = dataWithRand.filter(i => i._2 <= trainValTestSplit.trainFrac).map(_._1)
    lazy val valData =
      dataWithRand.filter(i =>
        i._2 > trainValTestSplit.trainFrac &&
        i._2 <= (trainValTestSplit.trainFrac + trainValTestSplit.validationFrac)
      ).map(_._1)

    lazy val testData =
      dataWithRand.filter(i =>
        i._2 > trainValTestSplit.trainFrac + trainValTestSplit.validationFrac
      ).map(_._1)
}
