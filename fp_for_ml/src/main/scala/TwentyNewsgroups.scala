package mlbigbook.app

import java.io.{ FileReader, BufferedReader, File }

object TwentyNewsgroups {

  import Corpus.fileAt

  val dataDir = fileAt(new File("."))("data")
  val dir20NG = fileAt(dataDir)("20_newsgroups")

  val newsgroups = IndexedSeq(
    "alt.atheism",
    "comp.graphics",
    "comp.os.ms-windows.misc",
    "comp.sys.ibm.pc.hardware",
    "comp.sys.mac.hardware",
    "comp.windows.x",
    "misc.forsale",
    "rec.autos",
    "rec.motorcycles",
    "rec.sport.baseball",
    "rec.sport.hockey",
    "sci.crypt",
    "sci.electronics",
    "sci.med",
    "sci.space",
    "soc.religion.christian",
    "talk.politics.guns",
    "talk.politics.mideast",
    "talk.politics.misc",
    "talk.religion.misc"
  )
}

class TwentyNewsgroups extends Corpus {

  import TwentyNewsgroups._
  import Corpus._

  val byNewsgroup =
    newsgroups
      .map { ng =>
        println(s"Loading $ng")
        (ng, loadDir(fileAt(dir20NG)(ng)))
      }

  override val documents: Traversable[String] =
    byNewsgroup
      .flatMap {
        case (_, fileContentPerDir) => fileContentPerDir
      }
      .filter { content => content.length > 4000 }
      .map { content => content.toLowerCase }
      .toTraversable

  println(s"${documents.size} documents across ${byNewsgroup.size} newsgroups")

}