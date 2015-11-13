package com.nitro.needle.neuralnet.mlp

import breeze.linalg.DenseMatrix

/**
 *
 * @param optimizer
 * @param l2RegLambda
 */
case class MlpExperimentConf(
                              optimizer: MlpOptimizer,
                              l2RegLambda: Double,
                              startingWeights: Option[Seq[DenseMatrix[Double]]] = None,
                              seed: Long = System.nanoTime,
                              earlyStopWindow: Option[Int] = None,
                              maxIter: Int
                              )