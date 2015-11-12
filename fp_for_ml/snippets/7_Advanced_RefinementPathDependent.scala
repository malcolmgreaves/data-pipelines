//
// [slide] Sub-typing and type refinement
//

// type refinement
def printShow2String1[S <: Show2 { type T = String }](x: S) = 
	println(s"""Showing: ${x.show("John Adams")}""")
// we can use any Show2, so long as it's type T = String

printShow2String1(s2)
printShow2String1(
	new Show2 { 
		type T = String
		def show(t: T) = t
	}
)

//
// [Slide] path-dependent types
//

trait Container {
	case class Record(x:Int)
}
// address abstract types with #
def printPathDepend(r: Container#Record) = 
	println(r)

object C extends Container
// address concrete types with .
printPathDepend(C.Record(422))

// type refinement & path-dependent types
def printShow2String2[S <: Show2 { type T = String }](x: S)(t: S#T) = 
	println(s"Showing: ${x.show(t)}")

printShow2String2(s2)("Officer Rabbit and I are going to watch you eat the whole bag.")

// no type refinement,
// but using path-dependent types on a value
def printShow2[S <: Show2](x: S)(t: x.T) = 
	println(s"""Showing: ${x.show(t)} """)

printShow2(s2)("goodbye universe")

val intShow = new Show2 {
	type T = Int
	def show(t: T) = t.toString
}
printShow2(intShow)(10)

// can assign anything to our unbound type T !!
case class Foobar(x:Int)
val showFoobar = new Show2 { 
	type T = Foobar
	def show(t: T) = s"Foobar has x=${t.x}"
}
printShow2(showFoobar)(Foobar(10))
