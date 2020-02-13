scalaVersion := "2.12.10"

name := "flink-processing"
organization := "ch.epfl.scala"
version := "1.0"

assemblyOutputPath in assembly := file("./app-assembly.jar")
assemblyMergeStrategy in assembly := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

libraryDependencies += "org.apache.flink" %% "flink-scala" % "1.9.1"
libraryDependencies += "org.apache.flink" %% "flink-connector-kafka" % "1.9.1"
libraryDependencies += "org.apache.flink" %% "flink-streaming-scala" % "1.9.1"
libraryDependencies += "org.apache.kafka" %% "kafka" % "2.4.0"
// Necessary for RPC
libraryDependencies += "org.apache.thrift" % "libthrift" % "0.9.3"