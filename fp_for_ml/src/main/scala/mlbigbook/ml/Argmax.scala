package mlbigbook.ml

import fif.Data

import Data.ops._
/**
 * Generic algorithm for finding the maximal argument.
 */
object Argmax {

  /**
   * Finds the maximal argument of `elements` in linear time. Uses the `Val`
   * typeclass as evidence of an argument's value.
   *
   * throws IllegalArgumentException Iff `elements` is empty.
   */
  def apply[T: Val, D[_]: Data](elements: D[T]): Option[T] =
    if (elements.isEmpty)
      None

    else
      Some(
        elements
          .reduce {
            case (a, b) =>
              if (implicitly[Val[T]].valueOf(a) > implicitly[Val[T]].valueOf(b))
                a
              else
                b
          }
      )
}

/**
 * Type class for giving a value to a type `X`.
 */
trait Val[T] {
  def valueOf(x: T): Double
}

object ValImplicits {

  implicit def firstIsNumeric[N: Numeric, T]: Val[(N, T)] =
    new Val[(N, T)] {
      override def valueOf(x: (N, T)): Double =
        implicitly[Numeric[N]].toDouble(x._1)
    }

  implicit def secondIsNumeric[N: Numeric, T]: Val[(T, N)] =
    new Val[(T, N)] {
      override def valueOf(x: (T, N)): Double =
        implicitly[Numeric[N]].toDouble(x._2)
    }

}