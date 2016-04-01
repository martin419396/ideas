name := """ideas"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.0"
)
