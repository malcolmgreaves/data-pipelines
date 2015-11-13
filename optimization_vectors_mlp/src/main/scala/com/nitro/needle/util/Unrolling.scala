package com.nitro.needle.util

import breeze.linalg.{DenseMatrix, DenseVector, reshape}
import shapeless.{:+:, CNil, Coproduct}

import scala.collection.mutable.ArrayBuffer

object Unrolling {

  type Unrollable = DenseVector[Double] :+: DenseMatrix[Double] :+: CNil

  def unroll(elems: Seq[Unrollable]): DenseVector[Double] = {

    def transform(elem: Unrollable): DenseVector[Double] = {

      (elem.select[DenseVector[Double]], elem.select[DenseMatrix[Double]]) match {

        case (Some(vec), None) => vec
        case (None, Some(mat)) => mat.toDenseVector
        // make compiler shut up, we're not anticipating this case
        case _ => ???
      }
    }

    val vecs = elems.map(transform)
    vecs.reduceLeft((state, elem) => DenseVector.vertcat(state, elem))
  }

  def roll(
            elems: DenseVector[Double],
            underlyingDims: Seq[Unrollable]
            ): Seq[Unrollable] = {

    val result = ArrayBuffer.empty[Unrollable]

    var current = 0

    for (i <- 0 until underlyingDims.size) {

      val curr = underlyingDims(i)

      (curr.select[DenseVector[Double]], curr.select[DenseMatrix[Double]]) match {

        case (Some(vec), None) => {
          val size = vec.iterableSize
          result += Coproduct[Unrollable](elems(current until current + size).toDenseVector)
          current += size
        }

        case (None, Some(mat)) => {

          val (rows, cols) = (mat.rows, mat.cols)
          val numEl = rows * cols
          val indices = current until current + numEl
          val a = elems(indices)
          val newMat = reshape(a, rows, cols)
          result += Coproduct[Unrollable](newMat)
          current += numEl

        }

        // make compiler shut up, we're not anticipating this case
        case _ => ???
      }
    }

    result.toSeq
  }

  def zerosFromUnrollable(underlying: Unrollable): Unrollable = {

    (underlying.select[DenseVector[Double]], underlying.select[DenseMatrix[Double]]) match {
      case (Some(vec), None) => Coproduct[Unrollable](DenseVector.zeros[Double](vec.activeSize))
      case (None, Some(mat)) => Coproduct[Unrollable](DenseMatrix.zeros[Double](mat.rows, mat.cols))
      case _ => ???
    }

  }
}
