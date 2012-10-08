import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "FareAlert"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.typesafe.akka" % "akka-actor" % "2.0.3",
      "net.databinder.dispatch" %% "dispatch-core" % "0.9.2"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
      // Add your own project settings here      
    )

}
