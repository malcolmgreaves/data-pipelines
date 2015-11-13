package com.nitro.needle.implicits

import org.apache.spark.rdd.RDD

import scala.language.higherKinds

import scala.util.Random

object SamplingImpl {

  implicit object TravSampling extends Sampling[Traversable] {

    override def sample[A](
                            d: Traversable[A]
                            )(
                            withReplacement: Boolean,
                            fraction: Double,
                            seed: Long
                            ): Traversable[A] = {

      val indexed = d.toIndexedSeq
      val rand = new Random(seed)

      /* Tail-recursive helper for sampling without replacement.
     Add picked element to acc and remove it from seq so
     it can't be chosen again.
     */
      @annotation.tailrec
      def collect(seq: IndexedSeq[A], size: Int, acc: List[A]): List[A] = {
        if (size == 0) acc
        else {
          val index = rand.nextInt(seq.size)
          collect(seq.updated(index, seq(0)).tail, size - 1, seq(index) :: acc)
        }
      }

      val sampleSize = (d.size * fraction).toInt
      // simple sampling with replacement
      def withRep: IndexedSeq[A] =
        for (i <- 1 to sampleSize)
          yield indexed(rand.nextInt(d.size))

      if (withReplacement)
        withRep
      else
        collect(indexed, sampleSize, Nil).toIndexedSeq
    }
  }

  implicit object RddSampling extends Sampling[RDD] {

    override def sample[A](
                            d: RDD[A]
                            )(
                            withReplacement: Boolean,
                            fraction: Double,
                            seed: Long
                            ): RDD[A] =
      d.sample(withReplacement = withReplacement, fraction = fraction, seed = seed)

  }

}