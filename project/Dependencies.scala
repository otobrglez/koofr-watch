import sbt._

object Dependencies {
  type Version = String
  type Modules = Seq[ModuleID]

  object Versions {
    val circe: Version      = "0.14.12"
    val zio: Version        = "2.1.17"
    val zioConfig: Version  = "4.0.4"
    val zioMetrics: Version = "2.3.1"
    val refined: Version    = "0.11.3"
  }

  lazy val zio: Modules = Seq(
    "dev.zio" %% "zio",
    "dev.zio" %% "zio-streams"
  ).map(_ % Versions.zio) ++ Seq(
    "dev.zio" %% "zio-test",
    "dev.zio" %% "zio-test-sbt",
    "dev.zio" %% "zio-test-magnolia",
    "dev.zio" %% "zio-test-scalacheck"
  ).map(_ % Versions.zio % Test) ++ Seq(
    "dev.zio" %% "zio-json"         % "0.7.42",
    "dev.zio" %% "zio-http"         % "3.2.0",
    "dev.zio" %% "zio-logging"      % "2.5.0",
    "dev.zio" %% "zio-prelude"      % "1.0.0-RC39",
    "dev.zio" %% "zio-interop-cats" % "23.1.0.5",
    "dev.zio" %% "zio-cli"          % "0.7.1"
  ) ++ Seq(
    "dev.zio" %% "zio-config",
    "dev.zio" %% "zio-config-magnolia",
    "dev.zio" %% "zio-config-typesafe",
    "dev.zio" %% "zio-config-refined"
  ).map(_ % Versions.zioConfig) ++ Seq(
    "dev.zio" %% "zio-metrics-connectors",
    "dev.zio" %% "zio-metrics-connectors-prometheus"
  ).map(_ % Versions.zioMetrics)

  lazy val logging: Modules = Seq(
    "dev.zio"       %% "zio-logging"        % "2.5.0",
    "dev.zio"       %% "zio-logging-slf4j2" % "2.5.0",
    "ch.qos.logback" % "logback-classic"    % "1.5.18"
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

  lazy val projectResolvers: Seq[MavenRepository] = Seq(
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Sonatype staging" at "https://oss.sonatype.org/content/repositories/staging",
    "Java.net Maven2 Repository" at "https://download.java.net/maven/2/",
    "zio-cli" at "https://repo1.maven.org/maven2/"
  ) ++ Resolver.sonatypeOssRepos("snapshots")
}
