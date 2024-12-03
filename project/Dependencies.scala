import sbt._

object Dependencies {
  type Version = String
  type Modules = Seq[ModuleID]

  object Versions {
    val http4s: Version        = "1.0.0-M40"
    val fs2: Version           = "3.11.0"
    val decline: Version       = "2.4.1"
    val log4cats: Version      = "2.7.0"
    val scalaTest: Version     = "3.2.19"
    val doobie: Version        = "1.0.0-RC5"
    val sentryLogback: Version = "7.18.1"
    val ical4j: Version        = "4.0.3"
    val quartz: Version        = "2.3.2"
    val circe: Version         = "0.15.0-M1"
    val flyway: Version        = "10.17.3"
    val postgresql: Version    = "42.7.4"
    val zio: Version           = "2.1.13"
    val zioLogging: Version    = "2.3.2"
    val zioConfig: Version     = "4.0.2"
    val refined: Version       = "0.11.2"
  }

  lazy val zio: Modules = Seq(
    "dev.zio" %% "zio",
    "dev.zio" %% "zio-streams"
  ).map(_ % Versions.zio) ++ Seq(
    "dev.zio" %% "zio-test",
    "dev.zio" %% "zio-test-sbt",
    "dev.zio" %% "zio-test-magnolia"
  ).map(_ % Versions.zio % Test) ++ Seq(
    "dev.zio" %% "zio-json"         % "0.7.3",
    "dev.zio" %% "zio-http"         % "3.0.1",
    "dev.zio" %% "zio-logging"      % "2.4.0",
    "dev.zio" %% "zio-prelude"      % "1.0.0-RC35",
    "dev.zio" %% "zio-interop-cats" % "23.1.0.3",
    "dev.zio" %% "zio-cli"          % "0.6.0+66-5b509020-SNAPSHOT"
  ) ++ Seq(
    "dev.zio" %% "zio-config",
    "dev.zio" %% "zio-config-magnolia",
    "dev.zio" %% "zio-config-typesafe",
    "dev.zio" %% "zio-config-refined"
  ).map(_ % Versions.zioConfig)

  lazy val logging: Modules = Seq(
    "dev.zio" %% "zio-logging",
    "dev.zio" %% "zio-logging-slf4j2"
  ).map(_ % Versions.zioLogging) ++ Seq(
    "ch.qos.logback" % "logback-classic" % "1.5.12"
  )

  lazy val refined: Modules = Seq(
    "eu.timepit" %% "refined",
    "eu.timepit" %% "refined-cats"
  ).map(_ % Versions.refined)

  lazy val circe: Modules = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circe)

  lazy val quartz: Modules = Seq(
    "org.quartz-scheduler" % "quartz"
  ).map(_ % Versions.quartz)

  lazy val projectResolvers: Seq[MavenRepository] = Seq(
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype staging" at "https://oss.sonatype.org/content/repositories/staging",
    "Java.net Maven2 Repository" at "https://download.java.net/maven/2/",
    "zio-cli" at "https://repo1.maven.org/maven2/"
  ) ++ Resolver.sonatypeOssRepos("snapshots")
}
