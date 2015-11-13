// use sbt-dev-settings to configure
import com.nitro.build._
import PublishHelpers._

// GAV coordinates
//
lazy val pName  = "needle"
lazy val semver = SemanticVersion(0, 0, 0, isSnapshot = true)
organization   := "com.gonitro"
name           := pName
version        := semver.toString

// scala & java
//
//                         :::   NOTE   :::
// we want to update to JVM 8 ASAP !
// since we know that we want to be able to use this stuff w/ Spark,
// we unfortunately have to limit ourselves to jvm 7.
// once this gets resolved, we'll update: 
// [JIRA Issue]     https://issues.apache.org/jira/browse/SPARK-6152
lazy val devConfig = {
  import CompileScalaJava._
  Config.spark
}
scalaVersion := "2.11.7"
CompileScalaJava.librarySettings(devConfig)
javaOptions := JvmRuntime.settings(devConfig.jvmVer)

// dependencies and their resolvers
//
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
libraryDependencies := Seq(
  // includes spark-core
  "io.malcolmgreaves"             %% "sparkmod"       % "0.1.1-SNAPSHOT",
   "com.github.mpilquist"         %% "simulacrum"     % "0.4.0",
  "org.scalanlp"                  %% "breeze"         % "0.11.2",
  // native libraries greatly improve performance, but increase jar sizes.
  "org.scalanlp"                  %% "breeze-natives" % "0.11.2",
  "org.scalanlp"                  %% "breeze-viz" 		% "0.11.2",
  "com.quantifind"                %% "wisp"           % "0.0.4",
  "org.apache.commons"            % "commons-lang3"   % "3.4",
  "org.scala-lang.modules"        %% "scala-pickling" % "0.10.1",
  "com.jsuereth"                  %% "scala-arm"      % "1.4",
  "com.github.scala-incubator.io" %% "scala-io-file"  %	"0.4.3-1",
  "com.chuusai"                   %% "shapeless"      % "2.2.5",
  // Testing
  "org.specs2"     %% "specs2"     % "2.4.15" % Test,
//  "org.scalatest"  %% "scalatest"  % "2.2.1"  % Test withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.12.1" % Test withSources() withJavadoc(),
  "org.scalatest"  %% "scalatest"  % "2.2.4"  % Test
)
resolvers := Seq(
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

// publishing settings
//
Publish.settings(
  repo = Repository.github("Nitro", pName),
  developers =
    Seq(
      Developer("mkolod",   "Marek Kolodziej", "mkolod@gmail.com",          new URL("https", "github.com", "/mkolod")),
      Developer("mgreaves", "Malcolm Greaves", "greaves.malcolm@gmail.com", new URL("https", "github.com", "/malcolmgreaves"))
    ),
  art = ArtifactInfo.sonatype(semver),
  lic = License.apache20
)

// test & misc. configuration
//
fork in Test              := false
parallelExecution in Test := true
