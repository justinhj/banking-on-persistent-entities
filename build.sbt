organization in ThisBuild := "org.justinhj"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.13.1"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test
val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "7.0.0"

lazy val `banking-on-persistent-entities` = (project in file("."))
  .aggregate(`banking-on-persistent-entities-api`, `banking-on-persistent-entities-impl`)

lazy val `banking-on-persistent-entities-api` = (project in file("banking-on-persistent-entities-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      playJsonDerivedCodecs
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
