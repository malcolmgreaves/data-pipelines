// 
// [slide] Let's take a look at Show again.
// 

// Show is a type class
// It says "for some type T, I know how to turn it into a String".
trait Show[T] {
	def show(t: T): String
}

object ImplicitShow {

	implicit object ShowString extends Show[String] {
		def show(t: String) = t
	}

	implicit object ShowInt extends Show[Int] {
		def show(t: Int) = 
			t.toString
	}

	// we have to deal with the fact that we need a new Show
	// instance for every type of Traversable, since Traversable
	// is itself parameterized 
	implicit def showTraversable[T]: Show[Traversable[T]] = 
		new Show[Traversable[T]] {
			def show(t: Traversable[T]) = 
				t.mkString("\t")
		}

}

// let's apply the evidence manually

println(ImplicitShow.ShowString.show("hello"))
println(ImplicitShow.ShowInt.show(-1252))
println(ImplicitShow.showTraversable.show(Traversable(1,2,3)))

// let's do it with implicit parameter

def printShowable1[T](t: T)(implicit s: Show[T]) =
	println(s.show(t))

// syntactic sugar: the following is equivalent to printShowable1
// the compiler re-writes it to the first form
// this "T : Show" is called a context bound
def printShowable2[T : Show](t: T) =
	println(implicitly[Show[T]].show(t))

import ImplicitShow._

printShowable1(10)
printShowable2(10)

printShowable1("hello")
printShowable2("hello")

printShowable1(Traversable(10, 20, 445))
printShowable2(Traversable(10, 20, 445))

println(s"For reference, this is the unmodifiable toString result: ${Traversable(10, 20, 445)}")