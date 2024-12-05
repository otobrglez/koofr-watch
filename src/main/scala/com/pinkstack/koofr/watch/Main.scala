package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.*
import io.circe.Json
import zio.*
import zio.Console.printLine
import zio.http.*
import zio.stream.ZStream

object Main extends ZIOAppDefault:
  // override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private def printJson(json: Json): Task[Unit] = zio.Console.printLine(json.toString())

  def run = (for
    koofr <- ZIO.service[KoofrService]
    // _     <- ZIO.fail(new RuntimeException("Not implemented"))

    webhooks <- ZIO.service[Webhooks]
    stream   <- ZStream
                  .serviceWithStream[ActivitiesOf](_.stream)
                  .mapZIO(webhooks.activitySink)
                  .runDrain
  yield ()).provide(
    Scope.default,
    WatchConfig.layer,
    KoofrService.layer,
    ActivitiesOf.layer,
    Webhooks.layer,
    Client.default
  )
