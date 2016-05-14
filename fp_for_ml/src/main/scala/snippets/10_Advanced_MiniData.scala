//package snippets

import language.higherKinds
import scala.reflect.ClassTag

import org.apache.spark.rdd.RDD
import org.apache.spark.{ SparkConf, SparkContext }

// This code is meant to be execued as a main application.
object AdvancedMiniData extends App {

  trait ToyData[D[_]] {
    def map[A, B: ClassTag](data: D[A])(f: A => B): D[B]
  }

  object ImplicitToyData {

    implicit object TravToyData extends ToyData[Traversable] {
      def map[A, B: ClassTag](data: Traversable[A])(f: A => B): Traversable[B] =
        data.map(f)
    }

    implicit object RddToyData extends ToyData[RDD] {
      def map[A, B: ClassTag](data: RDD[A])(f: A => B): RDD[B] =
        data.map(f)
    }

  }

  def mapExample[D[_]: ToyData](data: D[Int]): D[String] =
    implicitly[ToyData[D]].map(data) { (value: Int) => (value * 2).toString }

  import ImplicitToyData._

  val onTrav = mapExample(Traversable(1, 2, 3))
  println(s"Result on traversable:\t$onTrav")

  val sc = new SparkContext(
    new SparkConf()
      .setMaster(s"local[2]")
      .setAppName("toydata")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
  )
  val onRdd = mapExample(sc.parallelize(Seq(1, 2, 3)))
  println(s"Result on RDD:\t$onRdd")
  sc.stop()

}
