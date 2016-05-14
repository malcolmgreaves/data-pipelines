package mlbigbook.ml

import fif.{ TravData, Data }
import Data.ops._
import mlbigbook.data.SimpleVec

import scala.language.higherKinds

object NearestNeighbors {

  type NeighborhoodSize = Int

  def ranker[T: Vectorizeable, D[_]: Data](
    dist:     Distance,
    existing: D[T]
  ): Ranker[T] = {

    val vectorizer = implicitly[Vectorizeable[T]].vectorize _

    val vectorized =
      existing
        .map { original =>
          (original, vectorizer(original))
        }

    new Ranker[T] {
      def rank(t: T, limit: NeighborhoodSize): Traversable[(T, Double)] = {
        val tAsVector = vectorizer(t)
        vectorized
          .map {
            case (original, vector) =>
              (original, dist(vector, tAsVector))
          }
          .sortBy {
            case (_, distance) => distance
          }
          .take(limit)
      }
    }
  }

  def classifier[T: Vectorizeable, D[_]: Data, C](
    dist:     Distance,
    existing: D[(T, C)]
  ): NeighborhoodSize => Classifier[T, C] = {

    val rankerWithClasses = {

      val vectorizer = implicitly[Vectorizeable[T]].vectorize _

      implicit val vectorizerIgnoreC =
        new Vectorizeable[(T, Option[C])] {
          def vectorize(x: (T, Option[C])): SimpleVec =
            vectorizer(x._1)
        }

      ranker[(T, Option[C]), D](
        dist,
        existing.map {
          case (original, klass) => (original, Some(klass))
        }
      )
    }

    k =>
      new Classifier[T, C] {

        implicit val _ = TravData
        import ValImplicits._

        def classify(t: T): C = {

          val topK =
            rankerWithClasses.rank((t, None), k)
              .map {
                case ((original, Some(klass)), _) =>
                  (original, klass)
              }

          val allVotes = Counting.count[(T, C), Double, Traversable](topK)

          val weightedVotes =
            allVotes
              .map {
                case ((_, klass), voteCount) =>
                  (klass, voteCount)
              }
              .toTraversable

          Argmax(weightedVotes)
            .map {
              case (klass, _) => klass
            }
            .get
        }
      }
  }

}