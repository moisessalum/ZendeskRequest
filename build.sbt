ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.12"

val sparkVersion = "3.5.0"

lazy val root = (project in file("."))
  .settings(
    name := "ZendeskRequest",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M9",
      "com.softwaremill.sttp.client4" %% "upickle" % "4.0.0-M9",
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
    )
  )