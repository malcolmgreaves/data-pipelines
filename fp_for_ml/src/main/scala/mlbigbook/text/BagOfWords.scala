package mlbigbook.text

import fif.{ Data, TravData }
import Data.ops._
import mlbigbook.data.SimpleVec
import mlbigbook.ml.{ Vectorizeable, Counting }

object BagOfWords {

  type Tokenizer = String => IndexedSeq[String]

  def apply[D[_]: Data, T: Show](
    tokenize: Tokenizer
  )(
    documents: D[T]
  ): (D[(T, SimpleVec)], Vectorizeable[T]) = {

    val countWordsIn: T => Map[String, Long] =
      doc => {
        val tokens = tokenize(implicitly[Show[T]].show(doc))
        implicit val _ = TravData
        Counting.count[String, Long, Traversable](tokens)
      }

    val bothDocWordCount =
      documents
        .map { doc =>
          (doc, countWordsIn(doc))
        }

    val words: Set[String] =
      bothDocWordCount
        .map {
          case (_, wc) => wc
        }
        .aggregate(Set.empty[String])(
          (accumulating: Set[String], wc: Map[String, Long]) =>
            accumulating ++ wc.keySet,
          (a1: Set[String], a2: Set[String]) =>
            a1 ++ a2
        )

    val vectorFromWordCount: Map[String, Long] => SimpleVec = {

      val index2word: IndexedSeq[String] =
        words.toIndexedSeq

      val word2index: Map[String, Int] =
        index2word
          .zipWithIndex
          .toMap

      SimpleVecFromWordCount(
        word2index,
        index2word
      )
    }

    val bowVectors: D[(T, SimpleVec)] =
      bothDocWordCount
        .map {
          case (doc, wc) =>
            (doc, vectorFromWordCount(wc))
        }

    val v = new Vectorizeable[T] {
      override def vectorize(document: T): SimpleVec =
        vectorFromWordCount(countWordsIn(document))
    }

    (bowVectors, v)
  }

}