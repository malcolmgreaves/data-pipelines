import language.higherKinds

trait Data[D[_]] {
	def map[A, B](data: D[A])(f: A => B): D[B]
}

object ImplicitData {

	implicit object TravData extends Data[Traversable] {
		def map[A,B](data: Traversable[A])(f: A => B): Traversable[B] = 
			data.map(f)
	}

}

def mapExample[D[_] : Data](data: D[Int]): D[String] = 
	implicitly[Data[D]].map(data){ (value: Int) => (value * 2).toString }

import ImplicitData._
val x = mapExample(Traversable(1,2,3))
println(x)
