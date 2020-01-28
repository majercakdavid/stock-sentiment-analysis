scalaVersion := "2.12.10"

name := "flink-processing"
organization := "ch.epfl.scala"
version := "1.0"

libraryDependencies += "org.apache.flink" %% "flink-scala" % "1.9.1"
libraryDependencies += "org.apache.flink" %% "flink-connector-kafka" % "1.9.1"
libraryDependencies += "org.apache.flink" %% "flink-streaming-scala" % "1.9.1"
libraryDependencies += "org.apache.kafka" %% "kafka" % "2.4.0"