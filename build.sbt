organization in ThisBuild := "io.straight.ete"

name := "ete"

version in ThisBuild := "0.2"

scalacOptions in ThisBuild := Seq("-target:jvm-1.7", "-unchecked", "-deprecation", "-encoding", "utf8", "-feature", "-language:postfixOps", "-language:implicitConversions") // , "-Ydebug", "-verbose","-Yissue-debug") // -Ydebug

scalaVersion in ThisBuild := Version.Scala

resolvers in ThisBuild ++= Seq( 
      "Local Maven Repository" at Path.userHome.asFile.toURI.toURL + ".m2/repository",
      "Journalio Repo" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-releases",
      "Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "twitter repo" at "http://maven.twttr.com",
      "typesafe releases repo" at "http://repo.typesafe.com/typesafe/releases",
      "apache snapshots" at "http://repository.apache.org/content/groups/snapshots",  // commons-csv
      "spray repo" at "http://repo.spray.io/"
)
 
// net.virtualvoid.sbt.graph.Plugin.graphSettings

addCommandAlias("stage", ";assembly")

seq(Revolver.settings: _*)

// classpathTypes ~= (_ + "orbit")

fork in run := true

fork := true
