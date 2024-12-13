import com.typesafe.sbt.packager.docker.Cmd

import System.getenv
import scala.util.Try

val scala3Version = "3.6.1"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val envPort: Int  = Option(System.getenv("PORT")).flatMap(p => Try(Integer.parseInt(p)).toOption).getOrElse(4447)
lazy val koofrUsername = Try(getenv("KOOFR_USERNAME")).get
lazy val koofrPassword = Try(getenv("KOOFR_PASSWORD")).get

lazy val root = project
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .in(file("."))
  .settings(
    name         := "KoofrWatch",
    version      := "0.0.1",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-feature",
      "-language:implicitConversions",
      "-java-output-version",
      "21"
    ),
    libraryDependencies ++=
      Dependencies.zio ++ Dependencies.refined ++ Dependencies.circe ++ Dependencies.logging
  )
  .settings(
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .settings(
    Compile / mainClass              := Some("com.pinkstack.koofr.watch.Main"),
    assembly / mainClass             := Some("com.pinkstack.koofr.watch.Main"),
    reStart / mainClass              := Some("com.pinkstack.koofr.watch.Main"),
    assembly / assemblyJarName       := "koofr-watch.jar",
    assembly / assemblyMergeStrategy := {
      case PathList("module-info.class")                        =>
        MergeStrategy.discard
      case PathList("META-INF", "jpms.args")                    =>
        MergeStrategy.discard
      case PathList("META-INF", "io.netty.versions.properties") =>
        MergeStrategy.first
      case PathList("deriving.conf")                            =>
        MergeStrategy.last
      case PathList(ps @ _*) if ps.last endsWith ".class"       => MergeStrategy.last
      case x                                                    =>
        val old = (assembly / assemblyMergeStrategy).value
        old(x)
    }
  )
  .settings(
    Compile / discoveredMainClasses := Seq(),
    dockerExposedPorts              := Seq(envPort),
    dockerExposedUdpPorts           := Seq.empty[Int],
    dockerUsername                  := Some("otobrglez"),
    dockerUpdateLatest              := true,
    dockerRepository                := Some("ghcr.io"),
    dockerBaseImage                 := "azul/zulu-openjdk-alpine:21-latest",
    packageName                     := "koofr-watch",
    dockerCommands                  := dockerCommands.value.flatMap {
      case add @ Cmd("RUN", args @ _*) if args.contains("id") =>
        List(
          Cmd("LABEL", "maintainer Oto Brglez <otobrglez@gmail.com>"),
          Cmd("LABEL", "org.opencontainers.image.url https://github.com/otobrglez/koofr-watch"),
          Cmd("LABEL", "org.opencontainers.image.source https://github.com/otobrglez/koofr-watch"),
          Cmd("RUN", "apk add --no-cache bash curl"),
          Cmd("ENV", "SBT_VERSION", sbtVersion.value),
          Cmd("ENV", "SCALA_VERSION", scalaVersion.value),
          Cmd("ENV", "KOOFR_WATCH_VERSION", version.value),
          Cmd("ENV", "PORT", envPort.toString),
          add
        )
      case other                                              => List(other)
    }
  )

resolvers ++= Dependencies.projectResolvers
