package com.nitro.needle.activations

import breeze.linalg.{DenseVector, DenseMatrix}
import math.{exp, tanh}

object Nonlinearities {

  sealed trait Nonlinearity {
    def sigma(mat: DenseMatrix[Double]): DenseMatrix[Double]
    def sigma(vec: DenseVector[Double]): DenseVector[Double] =
      sigma(vec.toDenseMatrix).toDenseVector
    def sigmaPrime(mat: DenseMatrix[Double]): DenseMatrix[Double]
    def sigmaPrime(vec: DenseVector[Double]): DenseVector[Double] =
      sigmaPrime(vec.toDenseMatrix).toDenseVector
    override def toString: String = this.getClass.getName
  }

  case object Sigmoid extends Nonlinearity {

    override def sigma(mat: DenseMatrix[Double]): DenseMatrix[Double] =
      mat.map(d => 1.0/(1.0 + exp(-d)))

    override def sigmaPrime(mat: DenseMatrix[Double]): DenseMatrix[Double] =
      sigma(mat) :* (1.0 - sigma(mat))
  }

  case object Tanh extends Nonlinearity {

    override def sigma(mat: DenseMatrix[Double]): DenseMatrix[Double] =
      mat.map(d => tanh(d))

    override def sigmaPrime(mat: DenseMatrix[Double]): DenseMatrix[Double] =
      DenseMatrix.ones[Double](mat.rows, mat.cols) :- sigma(mat).map(x => math.pow(x, 2))
  }

  case object Linear extends Nonlinearity {

    override def sigma(mat: DenseMatrix[Double]): DenseMatrix[Double] =
      mat

    override def sigmaPrime(mat: DenseMatrix[Double]): DenseMatrix[Double] =
      DenseMatrix.ones[Double](mat.rows, mat.cols)
  }

//  // TODO: fix SoftMax
  case object SoftMax extends Nonlinearity {

    override def sigma(mat: DenseMatrix[Double]): DenseMatrix[Double] = {

      val denom = Sigmoid.sigma(mat).
        map(d => exp(d)).
        toDenseVector.
        toArray.
        sum

      mat.map(d => exp(d) / denom)
    }

    override def sigmaPrime(mat: DenseMatrix[Double]): DenseMatrix[Double] =
      ???
    }

}
