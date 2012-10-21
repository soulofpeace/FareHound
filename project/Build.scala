import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "FareHound"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.typesafe.akka" % "akka-actor" % "2.0.3",
      "net.databinder.dispatch" %% "dispatch-core" % "0.9.2",
      "net.debasishg" %% "redisclient" % "2.5",
      "org.apache.commons" % "commons-email" % "1.2",
      "com.esotericsoftware.kryo" % "kryo" % "2.19"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    )

}
