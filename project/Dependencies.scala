import sbt._
import sbt.librarymanagement.ModuleID

object Dependencies {

  private val config = Seq(
    "com.typesafe"           % "config"     % "1.4.3",
    "com.github.pureconfig" %% "pureconfig" % "0.17.6"
  )

  lazy val zio: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio"       % "2.0.21",
    "dev.zio" %% "zio-kafka" % "2.7.4"
  )

  private val tapir: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"   % "1.10.0",
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"        % "1.10.7",
    "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.10.7"
  )

  private val circeVersion = "0.14.1"

  private val circe = Seq(
    "io.circe" %% "circe-core"           % circeVersion,
    "io.circe" %% "circe-generic"        % circeVersion,
    "io.circe" %% "circe-generic-extras" % circeVersion
  )

  private val mongodb = Seq(
    "org.mongodb.scala" %% "mongo-scala-driver" % "4.6.0"
  )

  private val test = Seq(
    "org.scalatest" %% "scalatest"               % "3.2.18"  % Test,
    "org.mockito"   %% "mockito-scala-scalatest" % "1.17.31" % Test
  )

  private val guice = Seq(
    "net.codingwell" %% "scala-guice" % "7.0.0"
  )

  val application: Seq[ModuleID] = zio ++ tapir ++ circe ++ mongodb ++ config ++ guice ++ test
  val client: Seq[ModuleID]      = config ++ zio
}
