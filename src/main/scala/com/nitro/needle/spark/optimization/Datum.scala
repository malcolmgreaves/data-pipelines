package com.nitro.needle.spark.optimization

import breeze.linalg.DenseVector

/**
 * @author Marek Kolodziej
 */
case class Datum(target: Double, features: DenseVector[Double]) {
  override def toString: String =
    s"Datum(target = $target, features = $features)"
}
