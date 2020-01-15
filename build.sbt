organization in ThisBuild := "org.justinhj"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.1"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test

lazy val `banking-on-persistent-entities` = (project in file("."))
  .aggregate(`banking-on-persistent-entities-api`, `banking-on-persistent-entities-impl`, `banking-on-persistent-entities-stream-api`, `banking-on-persistent-entities-stream-impl`)

lazy val `banking-on-persistent-entities-api` = (project in file("banking-on-persistent-entities-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `banking-on-persistent-entities-impl` = (project in file("banking-on-persistent-entities-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings)
  .dependsOn(`banking-on-persistent-entities-api`)

lazy val `banking-on-persistent-entities-stream-api` = (project in file("banking-on-persistent-entities-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `banking-on-persistent-entities-stream-impl` = (project in file("banking-on-persistent-entities-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`banking-on-persistent-entities-stream-api`, `banking-on-persistent-entities-api`)
