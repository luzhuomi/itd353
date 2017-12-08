import sbt._
import Process._
import Keys._

name := "p8_storm"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Maven Repository" at "http://mvnrepository.com/artifact/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "clojars" at "https://clojars.org/repo"

resolvers += "luzhuomi github repo" at "https://raw.githubusercontent.com/luzhuomi/mavenrepo/master/"

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.3"
libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "4.0.3"

libraryDependencies += "com.github.velvia" %% "scala-storm" % "0.3.0-SNAPSHOT"

// libraryDependencies += "org.apache.storm" % "storm-core" % "0.9.3" % "provided" exclude("junit", "junit")
libraryDependencies += "org.apache.storm" % "storm-core" % "1.0.2" % "provided" exclude("junit", "junit")

scalacOptions ++= Seq("-feature", "-deprecation", "-Yresolve-term-conflict:package")

// resolvers ++= Seq("clojars" at "http://clojars.org/repo/",
//                  "clojure-releases" at "http://build.clojure.org/releases")

// seq(assemblySettings: _*)


assemblyMergeStrategy in assembly := {
    case PathList("log4j.properties") => MergeStrategy.discard
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.last // leiningen build files
}

