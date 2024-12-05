package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.Koofr.Activity
import io.circe.Json
import zio.Console.printLine
import zio.http.Header.{Accept, ContentType}
import zio.http.*
import zio.*
import zio.stream.*

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

  // TODO: This should be converted to a stream sink.
  def activitySink(activity: Activity): ZIO[Scope, Throwable, Unit] =
    ZIO.foreachDiscard(config.webhooks) { webhookUrl =>
      for
        response <-
          c.batched(
            Request.post(
              webhookUrl,
              // TODO: Adjust this so that the payload for Discord actually matches their format:
              // https://birdie0.github.io/discord-webhooks-guide/discord_webhook.html
              Body.fromString(Json.obj("content" -> activity.asJson.deepDropNullValues).noSpaces)
            )
          ).zipLeft(ZIO.logInfo(s"Webhook request sent to $webhookUrl"))

        // TODO: We don't care about the response. Only status code really. And even that can be ignored.
        _        <- response.body.asString.flatMap(printLine(_))
      yield ()
    }

object Webhooks:
  def layer = ZLayer.derive[Webhooks]
