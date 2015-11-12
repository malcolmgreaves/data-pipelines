package mlbigbook.text

import org.scalatest.FunSuite

class SimpleVecFromWordCountTest extends FunSuite {

  test("Making a SimpleVec from a word count works") {

    val vec =
      SimpleVecFromWordCount(
        Map(
          "hello" -> 0,
          "world" -> 1,
          "universe" -> 2
        ),
        IndexedSeq("hello", "world")
      )(
          Map(
            "hello" -> 44,
            "world" -> 10
          )
        )

    assert(vec.cardinality == 2)

    // out of bounds -> 0
    assert(vec.valueAt(-1241) == 0.0)
    assert(vec.valueAt(1241) == 0.0)

    assert(vec.valueAt(0) == 44.0)
    assert(vec.valueAt(1) == 10.0)
    assert(vec.valueAt(2) == 0.0)

    assert(vec.nonZeros == Traversable((0, 44.0), (1, 10.0)))
  }

}