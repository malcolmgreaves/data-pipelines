package mlbigbook.text

import mlbigbook.data.SimpleVec

object SimpleVecFromWordCount {

  def apply(
    word2index: Map[String, Int],
    index2word: IndexedSeq[String]
  )(
    wc: Map[String, Long]
  ): SimpleVec = {
    new SimpleVec {

      val cardinality =
        index2word.size

      val nonZeros =
        wc
          .toTraversable
          .map {
            case (word, count) =>
              (word2index(word), count.toDouble)
          }

      def valueAt(dim: Int): Double =
        if (dim >= 0 && dim < cardinality) {

          val word = index2word(dim)
          if (wc.contains(word))
            wc(word).toDouble
          else
            0.0

        } else {
          0.0
        }
    }
  }

}