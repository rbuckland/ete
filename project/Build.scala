import sbt._
import Keys._
import spray.revolver.RevolverPlugin._
import sbtassembly.Plugin._
import AssemblyKeys._

object Settings {
  val projectName = "ete"
  val buildSettings = Defaults.defaultSettings
}

object Version {
  val Scala = "2.10.3"
  val Akka = "2.3.0"
  val Spray = "1.3.1"
  val Jackson = "2.2.1"
}

/**
 * The SBT Build Object
 */
object RootBuild extends Build {

  import Settings._
  import Dependencies._

  lazy val eteRoot = Project(projectName, file("."))
    .aggregate(eteExtractor,eteToolsBackEnd)
    .settings(buildSettings: _*)

  lazy val eteExtractor = Project("ete-extractor", file("ete-extractor"))
    .settings(buildSettings: _*)
    .settings(libraryDependencies ++= (commonDeps ++ eteExtractorDeps).map(_.exclude("commons-logging", "commons-logging"))  )

  lazy val eteToolsBackEnd = Project("ete-tools-backend", file("ete-tools-backend"))
    .dependsOn(eteExtractor)
    .settings(buildSettings: _*)
    .settings(Revolver.settings: _*)
    .settings(assemblySettings: _*)
    .settings(libraryDependencies ++=
      (commonDeps ++ ioStraightFramework ++ subcut ++ ioStraightFramework
        ++ googleApiDep ++ sprayContribSession).map(_.exclude("commons-logging", "commons-logging"))
    )
}

/**
 * Our Project Dependencies
 */
object Dependencies {

  val ioStraightFramework = Seq(
    "io.straight.fw" %% "io-straight-fw" % "0.1" % "compile",
    "io.straight.dbtools" % "xls2db" % "0.3.5" % "compile",
    "com.h2database" % "h2" % "1.3.175" % "compile"

  )

  val sprayContribSession = Seq(
    "com.binarycamp.spray-contrib" % "spray-session" % "0.1.0-SNAPSHOT" % "compile"
  )

  val googleApiDep = Seq(
    "com.google.api.client" % "google-api-client" % "1.4.1-beta" % "compile"
  )

  val subcut = Seq(
    "com.escalatesoft.subcut" %% "subcut" % "2.0" % "compile"
  )

  val eteExtractorDeps = Seq (
    "net.sf.ehcache" % "ehcache-core" % "2.6.8" % "compile",
    "org.apache.poi" % "poi" % "3.9" % "compile",
    "org.apache.commons" % "commons-csv" % "1.0-SNAPSHOT" % "compile",
    "net.java.dev.inflector" % "inflector" % "0.7.0" % "compile", // see also https://github.com/flextao/inflector

    // ehcache wont compile without this (but we don't use it)
    "javax.transaction" % "jta" % "1.1" % "provided",

    // marshalling out to XML & JSON through StAX API
    "de.odysseus.staxon" % "staxon" % "1.3" % "compile",
    "de.odysseus.staxon" % "staxon-jackson" % "1.3" % "compile",
    "org.codehaus.woodstox" % "woodstox-core-asl" % "4.2.0" % "compile",


    // our database connection
    "org.apache.commons" % "commons-dbcp2" % "2.0" % "compile",

    // password encryption for the jdbc endpoints
    "org.jasypt" % "jasypt" % "1.9.2" % "compile",

    "com.h2database" % "h2" % "1.3.175" % "test"

    //    "com.beachape.filemanagement" %% "schwatcher" % "0.0.2" % "compile",
  )

  val commonDeps = Seq(

    // 
    // general Scala dependencies
    //
    "org.scala-lang" % "scala-reflect" % Version.Scala % "compile",
    "org.scalaz" %% "scalaz-core" % "7.0.6" % "compile",
    "com.typesafe.akka" %% "akka-actor" % Version.Akka % "compile",
    "org.scala-stm" %% "scala-stm" % "0.7" % "compile",

    //
    // Logging
    //
    "ch.qos.logback" % "logback-classic" % "1.0.9" % "compile",
    "com.typesafe.akka" %% "akka-slf4j" % Version.Akka % "compile",
    "com.typesafe.akka" %% "akka-actor" % Version.Akka % "compile",
    "org.slf4j" % "jcl-over-slf4j" % "1.7.5" % "compile",

    //
    // General Helper Libraries (Joda of course)
    //
    "org.apache.commons" % "commons-lang3" % "3.1" % "compile",
    "joda-time" % "joda-time" % "2.1" % "compile",
    "org.joda" % "joda-convert" % "1.2" % "compile",
    "org.joda" % "joda-money" % "0.8" % "compile",

    //
    // Test Dependencies
    //
    "org.scala-lang" % "scala-actors" % Version.Scala % "test",
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",

    // "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test",
    // sadly scalamock can't mock jdbc ResultSets..
    "org.easymock" % "easymock" % "3.2" % "test",

    "junit" % "junit" % "4.8" % "test"

    /*
     * Setup our excluded jars 
     */

  )
}
