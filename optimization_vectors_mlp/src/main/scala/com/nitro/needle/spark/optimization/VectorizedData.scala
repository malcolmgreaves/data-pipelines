package com.nitro.needle.spark.optimization

import breeze.linalg.DenseMatrix

/**
 * Group a bunch of examples into a feature matrix and a target vector,
 * instead of processing a feature vector and a target value at a time.
 * This will allow for vectorizing the linear algebra. When Breeze's
 * BLAS support is available (see https://github.com/fommil/netlib-java),
 * Breeze will execute linear algebra operations natively, benefiting from
 * lack of garbage collection, vectorization via SSE, etc.
 *
 * @author Marek Kolodziej
 *
 * @param targets
 * @param features
 */
case class VectorizedData(targets: DenseMatrix[Double], features: DenseMatrix[Double]) {
  override def toString: String =
    s"""VectorizedData(
       |target = $targets,
       |features = $features
       |)
       """.stripMargin
}