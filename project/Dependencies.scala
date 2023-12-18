import sbt._

object Dependencies {
  val scalaTest = "org.scalatest" %% "scalatest" % "3.2.17"
  val cats = "org.typelevel" %% "cats-effect" % "3.5.2"

  val doobieVersion = "1.0.0-RC2"
  val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion,
    "org.tpolecat" %% "doobie-scalatest" % doobieVersion % "test"
  )

  val tofuVersion = "0.12.0.1"
  val tofu = Seq(
    "tf.tofu" %% "tofu-logging" % tofuVersion,
    "tf.tofu" %% "tofu-logging-derivation" % tofuVersion,
    "tf.tofu" %% "tofu-logging-layout" % tofuVersion,
    "tf.tofu" %% "tofu-logging-logstash-logback" % tofuVersion,
    "tf.tofu" %% "tofu-logging-structured" % tofuVersion,
    "tf.tofu" %% "tofu-core-ce3" % tofuVersion,
    "tf.tofu" %% "tofu-doobie-logging-ce3" % tofuVersion
  )
  val tofuCirce = "tf.tofu" %% "derevo-circe" % "0.13.0"
  val http4sVersion = "0.23.24"
  val http4s = Seq(
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
  )
  val ioEstatico = "io.estatico" %% "newtype" % "0.4.4"

  val tapirVersion = "1.9.0"
  val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-derevo" % tapirVersion,
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
  )

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.17.4"

  val mock = Seq (
    "org.scalamock" %% "scalamock" % "5.1.0" % Test,
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.42" % Test
  )
}
