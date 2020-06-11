name := "p8_storm"

version := "1.0"

sbtVersion in Global := "1.3.12"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Maven Repository" at "http://mvnrepository.com/artifact/"

resolvers += "clojars" at "https://clojars.org/repo"


libraryDependencies += "org.apache.storm" % "storm-core" % "1.2.2" % "provided" exclude("junit", "junit")

scalacOptions ++= Seq("-feature", "-deprecation", "-Yresolve-term-conflict:package")

libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.3"
libraryDependencies += "org.twitter4j" % "twitter4j-stream" % "4.0.3"

scalacOptions ++= Seq("-feature", "-deprecation", "-Yresolve-term-conflict:package")

// resolvers ++= Seq("clojars" at "http://clojars.org/repo/",
//                  "clojure-releases" at "http://build.clojure.org/releases")

// seq(assemblySettings: _*)


assemblyMergeStrategy in assembly := {
    case PathList("log4j.properties") => MergeStrategy.discard
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case PathList("defaults.yaml", xs @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.last // leiningen build files
}

