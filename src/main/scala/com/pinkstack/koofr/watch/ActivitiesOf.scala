package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.Koofr.Activity
import com.pinkstack.koofr.watch.koofr.KoofrService
import zio.stream.ZStream
import zio.*

import java.time.{Instant, ZoneOffset}

final case class ActivitiesOf private (
  private val koofr: KoofrService
):
  private val limit         = 30
  private val fetchInterval = 30.seconds
  private val utc           = ZoneOffset.UTC
  private def now           = Instant.now().atZone(utc).toInstant.toEpochMilli

  def stream: ZStream[Any, Throwable, Activity] =
    ZStream
      .fromZIO(Ref.make(now))
      .flatMap { lastFetchRef =>
        ZStream.repeatZIOWithSchedule(
          for
            lastFetch  <- lastFetchRef.get
            activities <- koofr
                            .activities(limit, sinceTime = Some(lastFetch))
                            .retry(Schedule.exponential(10.seconds) && Schedule.recurs(3))
            _          <- lastFetchRef.setAsync(now)
          yield activities,
          Schedule.spaced(fetchInterval)
        )
      }
      .flatMap(ZStream.fromIterable)

object ActivitiesOf:
  def layer: ZLayer[WatchConfig & KoofrService, Nothing, ActivitiesOf] = ZLayer.scoped:
    for koofr <- ZIO.service[KoofrService]
    yield ActivitiesOf(koofr)
