package mlbigbook.app

import java.io.{FileReader, BufferedReader, File}

trait Corpus {
  val documents: Traversable[String]
}

object Corpus {

  def load(fi: File): String = {
    val br = new BufferedReader(new FileReader(fi))
    val sb = new StringBuilder()
    var line: String = br.readLine()
    while (line != null) {
      sb
        .append(line)
        .append("\n")
      line = br.readLine()
    }
    sb.toString()
  }

  // this version returns an empty corpus for a non-existing directory!
  def loadDir(d: File): Traversable[String] =
    Option(d.listFiles())
      .map { files =>
        files
          .map(load)
          .toTraversable
      }
      .getOrElse(Traversable.empty[String])

  def whitespaceTokenizer(s: String): IndexedSeq[String] =
    s.split("""\\s+""").toIndexedSeq

  def fileAt(parent: File)(parts: String*) =
    parts.foldLeft(parent)((f, p) => new File(f, p))
}

