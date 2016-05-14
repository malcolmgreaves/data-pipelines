package mlbigbook.ml

trait Ranker[T] {
  def rank(t: T, limit: Int): Traversable[(T, Double)]
}