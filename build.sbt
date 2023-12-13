ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "1.0"
ThisBuild / scalacOptions += "-Ymacro-annotations"

enablePlugins(
  UniversalPlugin,
  JavaAppPackaging,
  DockerPlugin
)

lazy val root = (project in file("."))
  .settings(
    name := "tiny-bank"
  )
  .aggregate(bank)

lazy val bank = (project in file("bank-module"))
  .settings(
    name := "bank-service",
    libraryDependencies += Dependencies.scalaTest % Test,
    libraryDependencies += Dependencies.cats,
    libraryDependencies ++= Dependencies.doobie,
    libraryDependencies ++= Dependencies.http4s,
    libraryDependencies ++= Dependencies.tofu,
    libraryDependencies += Dependencies.tofuCirce,
    libraryDependencies += Dependencies.ioEstatico,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.9.0",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.9.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.9.0",
      "com.softwaremill.sttp.tapir" %% "tapir-derevo" % "1.9.0",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.9.0",
    ),
    libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.4",
      scalacOptions += "-Ymacro-annotations",
    dependencyOverrides += "io.circe" %% "circe-core" % "0.14.5"
  )
  .withId("bank")
