package mlbigbook.ml

import mlbigbook.data.SimpleVec
import SimpleVec._

/**
 * Type for a distance function.
 *
 * Evaluated value must be non-negative and obey the rules of a distance in a metric space.
 * These properties are:
 *   ** non-negativity : \forall x,y d(x,y) >= 0
 *   ** equality :  d(x,y) = 0 <==> x = y
 *   ** symmetry : \forall x,y d(x,y) = d(y,x)
 *   ** triangle inequality : \forall x,y,z d(x,y) <= d(x,z) + d(z,y)
 */
trait Distance {
  def apply(v1: SimpleVec, v2: SimpleVec): Double
}

object Euclidian extends Distance {

  override def apply(v1: SimpleVec, v2: SimpleVec): Double =
    Math.sqrt(
      v1.zip(v2)
        .foldLeft(0.0)({
          case (d, (_, value1, value2)) =>
            val difference = value1 - value2
            d + difference * difference
        })
    )
}

object Manhattan extends Distance {

  override def apply(v1: SimpleVec, v2: SimpleVec): Double =
    v1.zip(v2)
      .foldLeft(0.0)({
        case (d, (_, value1, value2)) =>
          d + Math.abs(value1 - value2)
      })
}

/**
 * Computes the cosine distance between two vectors, which is defined as:
 *
 *                  | v1 * v2 |
 *       1.0  -  -----------------
 *                  |v1| * |v2|
 *
 * where * is dot product and |...| is L1 norm.
 */
object Cosine extends Distance {

  def apply(v1: SimpleVec, v2: SimpleVec): Double =
    1.0 - Math.abs(dotProduct(v1, v2)) / (absoluteValue(v1) * absoluteValue(v2))
}

object Chebyshev extends Distance {

  def apply(v1: SimpleVec, v2: SimpleVec): Double =
    v1.zip(v2)
      .foldLeft(Option.empty[Double])({
        case (max, (_, value1, value2)) =>

          val absDiff = Math.abs(value1 - value2)
          max match {

            case m @ Some(maxValue) =>
              if (maxValue < absDiff)
                Some(absDiff)
              else
                m

            case None =>
              Some(absDiff)
          }
      }) match {

        case Some(maximumFound) =>
          maximumFound

        case None =>
          0.0
      }
}

object BrayCurtis extends Distance {

  def apply(v1: SimpleVec, v2: SimpleVec): Double = {

    val (sumAbsPairwiseDiff, sumAbsPairwiseSum) =
      v1.zip(v2)
        .foldLeft((0.0, 0.0))({
          case ((absDiffSum, absPairSum), (_, value1, value2)) =>
            (absDiffSum + Math.abs(value1 - value2), absPairSum + Math.abs(value1 + value2))
        })

    sumAbsPairwiseDiff / sumAbsPairwiseSum
  }
}

case object Canberra extends Distance {

  def apply(v1: SimpleVec, v2: SimpleVec): Double =
    v1.zip(v2)
      .foldLeft(0.0)({
        case (sum, (_, value1, value2)) =>
          val absDiff = Math.abs(value1 - value2)
          val indivAbsSum = Math.abs(value1) + Math.abs(value2)
          sum + (absDiff / indivAbsSum)
      })
}

case object MinkowskiMaker {

  def apply(p: Int): Distance =
    new Distance {

      override def apply(v1: SimpleVec, v2: SimpleVec): Double =
        raiseTo1OverP(
          v1.zip(v2)
            .foldLeft(0.0)({
              case (sum, (_, value1, value2)) =>
                sum + raiseAbsDiffToP(value1, value2)
            })
        )

      private[this] def raiseAbsDiffToP(v1: Double, v2: Double): Double =
        Math.pow(Math.abs(v1 - v2), p)

      private[this] val pInv = 1.0 / p

      private[this] def raiseTo1OverP(x: Double): Double =
        Math.pow(x, pInv)
    }
}