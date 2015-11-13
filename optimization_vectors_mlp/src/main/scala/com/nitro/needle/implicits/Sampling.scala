package com.nitro.needle.implicits

import scala.language.higherKinds

trait Sampling[D[_]] extends Serializable  {

  def sample[A](d: D[A])(withReplacement: Boolean, fraction: Double, seed: Long): D[A]
}

object Sampling {

  def apply[D[_] : Sampling]: Sampling[D] =
    implicitly[Sampling[D]]

}
