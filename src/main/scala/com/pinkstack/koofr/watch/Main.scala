package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.*
import io.circe.Json
import zio.*
import zio.Console.printLine
import zio.http.*
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault:
  import ActivityOps.*
  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private def printJson(json: Json): Task[Unit] = zio.Console.printLine(json.toString())

  def run = (for
    koofr      <- ZIO.service[KoofrClient]
    activities <- koofr.activities()
    _          <- printLine(activities.map(_.print).mkString("\n"))
  yield ()).provide(
    WatchConfig.layer,
    KoofrClient.layer,
    Client.default
  )
