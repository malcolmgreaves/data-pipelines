// use sbt-dev-settings to configure
import com.nitro.build._
import PublishHelpers._

// GAV coordinates
//
organization := "io.malcolmgreaves"
name         := "fp_and_ml"
version      := "0.0.0-SNAPSHOT"

// dependencies and their resolvers
//
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
libraryDependencies ++= Seq(
  // main 
  "org.scalanlp"      %% "breeze"   % "0.11.2",
  "org.spire-math"    %% "spire"    % "0.11.0",
  "com.quantifind"    %% "wisp"     % "0.0.4",
  "io.malcolmgreaves" %% "sparkmod" % "1.0.0",
  // test
  "org.scalatest"     %% "scalatest"     % "2.2.4" % Test
)
resolvers ++= Seq(
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

// scala compilation and java runtime settings
//
lazy val devConfig = {
  import CompileScalaJava._
  Config.spark.copy(
    scala = Config.spark.scala.copy(
      genBBackend = false
    )
  )
}
scalaVersion := "2.11.7"
CompileScalaJava.librarySettings(devConfig)
javaOptions := JvmRuntime.settings(devConfig.jvmVer)


// testing and code coverage settings
fork in Test              := true
parallelExecution in Test := false

