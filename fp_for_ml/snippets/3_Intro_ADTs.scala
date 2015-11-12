//
// [slide] using abstract data types
//

// using the built-in Option type
// indicating that it may or may not have a value
// or, now you never have to deal with null!

def printOpt(x: Option[String]) = 
	x match {

		case Some(str) => 
			println(s"Hey! We have something here, it is: $str")

		case None =>
			println(s"Oh...we have nothing.")
	}

printOpt(Some("hello!"))
printOpt(Some("pretty neat!"))
printOpt(None)

//
// [slide] making our own abstract data types
//

type UserName = String
type UUID = Long
case class User(name: UserName, id: UUID)

sealed trait Request
case class FetchInformation(u: User) extends Request
case object SystemStatus extends Request

type Message = String
case class PostToFeed(u: User, m: Message) extends Request

case class Send(from: User, m: Message, to: User) extends Request

def handle(r: Request): Unit = 
	r match {

		case FetchInformation(user) =>
			println(s"Fetching information for ${user}")

		case SystemStatus =>
			println(s"System OK. Current time: ${System.currentTimeMillis}ms")

		case PostToFeed(user, message) =>
			println(s"""Posting "$message" to feed for $user""")

		case Send(from, message, to) =>
			println(s"""Sending a message from user $from to user $to : "$message" """)
	}

handle(SystemStatus)
val u1 = User("bobby", 125151245l)
handle(FetchInformation(u1))
handle(PostToFeed(u1, "omgz! I'm on the interwebzzzz"))
handle(PostToFeed(u1, "I am l33t"))
val u2 = User("sally", 666363l)
handle(Send(u2, "Please be quiet.", u1))
handle(SystemStatus)


// the following fails at compile time
// precisely because we're not handling every type of Request
// (this is ** awesome ** )
def handleBad(r: Request): Unit = 
	r match {
		case FetchInformation(user) =>
			println(s"Fetching information for ${user}")
	}


// what if we didn't have "sealed" ?
trait UnsafeRequest
case class UnsafeFetchInformation(u: User) extends UnsafeRequest
case object UnsafeSystemStatus extends UnsafeRequest

// ahh! this compiles
def unsafeHandle(r: UnsafeRequest): Unit = 
	r match {
		case UnsafeFetchInformation(user) =>
			println(s"Fetching information for ${user}")
	}

// oh no :(
unsafeHandle(UnsafeSystemStatus) // fails with MatchError
