package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.Koofr.Activity
import io.circe.Json
import zio.*
import zio.http.*
import zio.http.Header.{Accept, ContentType}
import zio.stream.*

trait Webhooks:
  private val defaultChunkSize = 100
  private val defaultWithin    = 1.minute
  private val defaultMaxLength = 1900

  def sendMessage(message: String): ZIO[Scope, Throwable, Unit]

  def pipeline(
    chunkSize: Option[Int] = None,
    maxLength: Option[Int] = None
  ): ZPipeline[Scope, Throwable, Activity, Unit] =
    ZPipeline
      .map[Activity, (Activity, Message)](activity => activity -> Message.fromActivity(activity))
      .map((activity, message) => activity -> (message -> message.length.toLong))
      .groupedWithin(chunkSize.getOrElse(defaultChunkSize), defaultWithin)
      .map(chunk => groupByMaxLength(chunk, maxLength = maxLength))
      .map(_.map(chunk => Message.fromActivities(chunk.map(_._1))))
      .flattenChunks
      .mapZIO(sendMessage)

  private def groupByMaxLength(
    remaining: Chunk[(Activity, (Message, Long))],
    current: Chunk[(Activity, (Message, Long))] = Chunk.empty,
    size: Long = 0L,
    maxLength: Option[Int] = None
  ): Chunk[Chunk[(Activity, (Message, Long))]] = remaining.headOption match
    case Some(combo @ (activity, activityMessage @ (_, messageLength)))
        if size + messageLength <= maxLength.getOrElse(defaultMaxLength) =>
      groupByMaxLength(remaining.drop(1), current :+ combo, size + messageLength, maxLength)
    case Some(_) if current.nonEmpty                                    =>
      current +: groupByMaxLength(remaining, maxLength = maxLength)
    case Some(combo @ (activity, activityMessage @ (_, messageLength))) =>
      Chunk(Chunk(combo)) ++ groupByMaxLength(remaining.drop(1), maxLength = maxLength)
    case None if current.nonEmpty                                       => Chunk(current)
    case None                                                           => Chunk.empty

final case class WebhooksLive private (
  private val client: Client,
  private val config: WatchConfig
) extends Webhooks:
  import io.circe.generic.auto.*

  private val c = client.addHeaders(
    Headers(Accept(MediaType.application.json), ContentType(MediaType.application.`json`))
  )

  // TODO: This should be converted to a stream sink.
  def sendMessage(message: String): ZIO[Scope, Throwable, Unit] =
    ZIO.foreachDiscard(config.webhooks) { webhookUrl =>
      for
        response <-
          c.batched(
            Request.post(
              webhookUrl,
              Body.fromString(Json.obj("content" -> Json.fromString(message)).noSpaces)
            )
          ).zipLeft(ZIO.logInfo(s"Webhook request sent to $webhookUrl"))
        _        <- response.body.asString.logError("Boom.")
      yield ()
    }
object WebhooksLive:
  def layer: ZLayer[Client & WatchConfig, Any, WebhooksLive] = ZLayer.derive[WebhooksLive]

object Webhooks:
  def layer: ZLayer[Client & WatchConfig, Any, Webhooks] = WebhooksLive.layer
