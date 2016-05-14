package snippets

object AdvancedImplicits extends App {

  //
  // [slide] Implicit parameters
  //

  import java.io.File

  // What will be used as an implicit parameter later...
  case class FileContext(
    training:          File,
    test:              File,
    heldOutValidation: File
  )

  // simple interface for a Model: can predict on a file and get Accuracy
  trait Model {
    type Accuracy = Double
    def predict(f: File): Accuracy
  }

  // simple interface for a trainer:
  // uses training data and a parameter to make a Model
  trait Trainer {
    def learn(f: File)(parameter: Double): Model
  }

  // Simulates a real ML experiment.
  // Trains a model on the same data with different parameter values.
  // Finds the best one according to the testing data.
  // Returns this model and its performance on the held-out data.
  def experiment(t: Trainer, parameters: Seq[Double], fc: FileContext) = {

    val fixedTrainer = t.learn(fc.training) _

    val bestModelOnTest =
      parameters
        .map { fixedTrainer }
        .map { model =>
          (model, model.predict(fc.test))
        }
        .reduce[(Model, Model#Accuracy)] {
          case (first @ (model1, accuracy1), second @ (model2, accuracy2)) =>
            if (accuracy1 > accuracy2)
              first
            else
              second
        }
        ._1

    (bestModelOnTest, bestModelOnTest.predict(fc.heldOutValidation))
  }

  // the following 2 methods are very similar, but slightly different

  def experimentCurried(t: Trainer, parameters: Seq[Double])(fc: FileContext) =
    experiment(t, parameters, fc)

  def experimentImplicits(t: Trainer, parameters: Seq[Double])(implicit fc: FileContext) =
    experiment(t, parameters, fc)

  // lets use this stuff in a trival implementation
  object Explore {

    val randoTrainer = new Trainer {
      import scala.util.Random
      def learn(f: File)(ignored: Double) =
        new Model {
          def predict(f: File) =
            Random.nextDouble * 100.0
        }
    }

    val parameters = (1 to 10).map(_.toDouble)

    implicit val fc = FileContext(
      new File("training"),
      new File("test"),
      new File("heldOutValidation")
    )

    def result0 =
      experiment(randoTrainer, parameters, fc)

    def result1 =
      experimentCurried(randoTrainer, parameters)(fc)

    // does implicit search for a FileContext
    // will find "fc" above since it is * in the same scope *
    def result2 =
      experimentImplicits(randoTrainer, parameters)

    // also totally valid - supply the implicit as if it was another parameter!
    def result3 =
      experimentImplicits(randoTrainer, parameters)(fc)

  }
}
