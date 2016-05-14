package mlbigbook.ml

trait Classifier[T, C] {
  def classify(t: T): C
}