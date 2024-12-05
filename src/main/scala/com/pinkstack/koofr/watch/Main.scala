package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.*
import zio.*
import zio.http.*
import zio.logging.backend.SLF4J
import zio.stream.ZStream

object Main extends ZIOAppDefault:
  override val bootstrap = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private def program = for
    koofr    <- ZIO.service[KoofrService]
    webhooks <- ZIO.service[Webhooks]
    _        <- ZStream.serviceWithStream[ActivitiesOf](_.stream).mapZIO(webhooks.activitySink).runDrain
  yield ()

  def run = program.provide(
    Scope.default,
    WatchConfig.layer,
    KoofrService.layer,
    ActivitiesOf.layer,
    Webhooks.layer,
    Client.default
  )
