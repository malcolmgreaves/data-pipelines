package com.nitro.needle.neuralnet.common

case class TrainValTestSplit(trainFrac: Double, validationFrac: Double) {
  require(trainFrac + validationFrac <= 1.0)
  val testFrac = 1.0 - trainFrac - validationFrac
}
