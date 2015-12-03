package mlbigbook.text

import fif.{ Data, TravData }
import Data.ops._
import mlbigbook.data.SimpleVec
import mlbigbook.ml.{ Vectorizeable, Counting }

import scala.language.higherKinds

object BagOfWords {

  type Tokenizer = String => IndexedSeq[String]

  def apply[D[_]: Data, T: Show](
    tokenize: Tokenizer
  )(
    documents: D[T]
  ): (D[(T, SimpleVec)], Vectorizeable[T]) = {

    val countWordsIn: T => Map[String, Long] =
      doc => {
        val tokens =
          tokenize(implicitly[Show[T]].show(doc))
            .filter(word => !StopWords.english.contains(word))
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

object StopWords {

  val english =
    """a
    |about
    |above
    |after
    |again
    |against
    |all
    |am
    |an
    |and
    |any
    |are
    |aren't
    |as
    |at
    |be
    |because
    |been
    |before
    |being
    |below
    |between
    |both
    |but
    |by
    |can't
    |cannot
    |could
    |couldn't
    |did
    |didn't
    |do
    |does
    |doesn't
    |doing
    |don't
    |down
    |during
    |each
    |few
    |for
    |from
    |further
    |had
    |hadn't
    |has
    |hasn't
    |have
    |haven't
    |having
    |he
    |he'd
    |he'll
    |he's
    |her
    |here
    |here's
    |hers
    |herself
    |him
    |himself
    |his
    |how
    |how's
    |i
    |i'd
    |i'll
    |i'm
    |i've
    |if
    |in
    |into
    |is
    |isn't
    |it
    |it's
    |its
    |itself
    |let's
    |me
    |more
    |most
    |mustn't
    |my
    |myself
    |no
    |nor
    |not
    |of
    |off
    |on
    |once
    |only
    |or
    |other
    |ought
    |our
    |ours	ourselves
    |out
    |over
    |own
    |same
    |shan't
    |she
    |she'd
    |she'll
    |she's
    |should
    |shouldn't
    |so
    |some
    |such
    |than
    |that
    |that's
    |the
    |their
    |theirs
    |them
    |themselves
    |then
    |there
    |there's
    |these
    |they
    |they'd
    |they'll
    |they're
    |they've
    |this
    |those
    |through
    |to
    |too
    |under
    |until
    |up
    |very
    |was
    |wasn't
    |we
    |we'd
    |we'll
    |we're
    |we've
    |were
    |weren't
    |what
    |what's
    |when
    |when's
    |where
    |where's
    |which
    |while
    |who
    |who's
    |whom
    |why
    |why's
    |with
    |won't
    |would
    |wouldn't
    |you
    |you'd
    |you'll
    |you're
    |you've
    |your
    |yours
    |yourself
    |yourselves""".stripMargin.split("\n").toSet

}