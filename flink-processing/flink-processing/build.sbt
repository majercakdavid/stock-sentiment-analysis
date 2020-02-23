scalaVersion := "2.12.10"

name := "flink-processing"
organization := "ch.epfl.scala"
version := "1.0"

assemblyOutputPath in assembly := file("./app-assembly.jar")
assemblyMergeStrategy in assembly := {
  case PathList("reference.conf")          => MergeStrategy.concat
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _                                   => MergeStrategy.first
}

libraryDependencies += "org.apache.flink" %% "flink-scala" % "1.10.0"
libraryDependencies += "org.apache.flink" %% "flink-connector-kafka" % "1.10.0"
libraryDependencies += "org.apache.flink" %% "flink-streaming-scala" % "1.10.0"
libraryDependencies += "org.apache.kafka" %% "kafka" % "2.4.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
libraryDependencies += "com.typesafe" % "config" % "1.2.1"

// Include config directory in build
Runtime / unmanagedClasspath += baseDirectory.value / "config"
