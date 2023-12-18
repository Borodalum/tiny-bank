ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "1.0"

enablePlugins(
  UniversalPlugin,
  JavaAppPackaging,
  DockerPlugin
)

lazy val root = (project in file("."))
  .settings(
    name := "tiny-bank",
    libraryDependencies += Dependencies.scalaTest % Test,
    libraryDependencies += Dependencies.cats,
    libraryDependencies ++= Dependencies.doobie,
    libraryDependencies ++= Dependencies.http4s,
    libraryDependencies ++= Dependencies.tofu,
    libraryDependencies += Dependencies.tofuCirce,
    libraryDependencies += Dependencies.ioEstatico,
    libraryDependencies ++= Dependencies.tapir,
    libraryDependencies += Dependencies.pureConfig,
    libraryDependencies ++= Dependencies.mock,
    dependencyOverrides += "io.circe" %% "circe-core" % "0.14.5"
  )

scalacOptions += "-Ymacro-annotations"
Docker / packageName := "tiny-bank"
dockerBaseImage := "openjdk:17-jdk-slim"
dockerExposedPorts ++= Seq(1234)