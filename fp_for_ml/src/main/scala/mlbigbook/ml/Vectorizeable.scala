package mlbigbook.ml

import mlbigbook.data.SimpleVec

trait Vectorizeable[T] {
  def vectorize(t: T): SimpleVec
}