package snippets

// This code is meant to be inspected and executed in the REPL in chunks.
object IntroTypeCaseClassesScope {

  //
  // [slide] case class and type alias
  //

  // type aliases
  type UniqueId = Int
  type UserName = String

  // immutable data structures
  case class Record(id: UniqueId, name: UserName)
  val r = Record(1251251245, "Rachel")

  // named arguments for classes too
  Record(id = 1251251245, name = "Rachel")

  // no "setters", use copy instead
  val newR = r.copy(name = "Bono")

  // auto-generated with case class: equals, hashCode 
  // these implementations are structural not reference!
  newR == r
  newR.hashCode
  r.hashCode
  // when do you really ever want reference equality anyway?
  val rCopy = Record(1251251245, "Rachel")
  rCopy == r
  rCopy.hashCode == r.hashCode

  //
  // [slide] scope
  //

  // cury brances denote scope
  val scope = {
    val a = 11
    val b = 22
    val c = 33
    a * b + c
  }
  scope == 275
  // pure things have referential transparency
  scope == {
    val a = 11
    val b = 22
    val c = 33
    a * b + c
  }

  // nest to your heart's desire!
  val nestedScopeOk = {
    val a = 9000
    val b = {
      val c = 10
      val d = 22
      c + d
    }
    a + b
  }

  //
  // [slide] sane scoping rules
  //

  /*

  // one cannot reference something in a different scope
  val nestedScopeFails1 = {
    val a = 9000
    val b = {
      val c = 10
      val d = 22
      c + d
    }
    // illegal, c and d are not in the same scope!
    (a + c + d) * b
  } // fails

  // one cannot reference something before its defined!
  val nestedScopeFails2 = {
    val a = b
    val b = {
      val c = 10
      val d = 22
      c + d
    }
    a + b
  } // fails

  */

  // but this is where we can use lazy vals !
  val lazyScope = {
    // works precisely because we are delaying the evaluation of both a and b
    // until we need them, which is after we have fulled defined _both_ of them
    lazy val a = b
    lazy val b = {
      val c = 10
      val d = 22
      c + d
    }
    // at this point, a and b are defined, so it is safe to use both
    a + b
  }

}