package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.Koofr.Activity
import io.circe.Json
import zio.Console.printLine
import zio.http.Header.{Accept, ContentType}
import zio.http.*
import zio.*

final case class Webhooks private (
  private val client: Client,
  private val config: WatchConfig
):
  import io.circe.generic.auto.*
  import io.circe.syntax.*

  private val c = client.addHeaders(
    Headers(
      Accept(MediaType.application.json),
      ContentType(MediaType.application.`json`)
    )
  )

  def activitySink(activity: Activity): ZIO[Scope, Throwable, Unit] =
    ZIO.foreachDiscard(config.webhooks) { webhookUrl =>
      for
        response <-
          c.batched(
            Request.post(
              webhookUrl,
              Body.fromString(
                Json.obj("content" -> activity.asJson.deepDropNullValues).noSpaces
              )
            )
          ).debug("Webhook request:")
        _        <- response.body.asString.flatMap(printLine(_))
      yield ()
    }

object Webhooks:
  def layer = ZLayer.derive[Webhooks]
