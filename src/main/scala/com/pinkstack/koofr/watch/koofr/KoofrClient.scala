package com.pinkstack.koofr.watch.koofr

import cats.instances.unit
import com.pinkstack.koofr.watch.WatchConfig
import io.circe.parser.parse as jsonParse
import io.circe.{Decoder, Json}
import zio.*
import zio.http.*
import zio.http.Header.{Accept, ContentType}
import eu.timepit.refined.auto.autoUnwrap
import zio.metrics.Metric
import zio.metrics.MetricKeyType.Histogram

import java.time.temporal.ChronoUnit
import java.util.UUID
import scala.util.control.NoStackTrace

enum KoofrError(message: String) extends Throwable(message) with NoStackTrace:
  case APIError(status: Status)   extends KoofrError(s"Koofr API error: $status")
  case JsonError(message: String) extends KoofrError(s"JSON parsing error: $message")

trait KoofrService:
  protected val activitiesPageLimit: Int = 20
  def activities(
    limit: Int = activitiesPageLimit,
    startIdExclusive: Option[Long] = None,
    startTime: Option[Long] = None,
    sinceTime: Option[Long] = None,
    mountId: Option[UUID] = None,
    categories: Set[String] = Set.empty
  ): Task[Koofr.Activities]

final case class KoofrClient private (
  private val client: Client
) extends KoofrService:
  import Koofr.*
  import KoofrError.*

  private val successCounter = Metric.counter("koofr_requests_success_cnt")
  private val failureCounter = Metric.counter("koofr_requests_failure_cnt")
  private val itemsCounter   = Metric.counter("koofr_collected_items_cnt")

  private val responseLatency = Metric.histogram(
    name = "koofr_response_latency_seconds",
    description = "Response latency in seconds",
    Histogram.Boundaries.exponential(0.005, 2.0, 10) // Buckets: 5ms, 10ms, 20ms, ..., ~5s
  )

  private val timer =
    Metric.timer(
      name = "koofr_requests_duration_ms",
      chronoUnit = ChronoUnit.MILLIS,
      boundaries = Chunk.iterate(1.0, 10)(_ + 1.0)
    )

  private def handleResponse(response: Response): Task[Json] = for
    body <- response.body.asString
    _    <- ZIO.when(response.status.isError)(ZIO.fail(APIError(response.status)))
    json <- ZIO.fromEither(jsonParse(body)).mapError(pf => JsonError(pf.getMessage))
  yield json

  private def decodeResponseAs[A](response: Response, lense: Json => Json = identity)(using Decoder[A]): Task[A] =
    handleResponse(response).flatMap(json => ZIO.fromEither(lense(json).as[A]))

  private def getAs[A](request: Request, lense: Json => Json = identity)(using Decoder[A]): Task[A] = for
    response <- client.batched(request) @@ timer.trackDuration
    body     <-
      response.status match
        case Status.Ok | Status.Created => successCounter.increment &> decodeResponseAs[A](response, lense)
        case _                          => failureCounter.increment &> ZIO.fail(APIError(response.status))
  yield body

  private def getPathAs[A](path: String, lense: Json => Json = identity)(using Decoder[A]): Task[A] =
    getAs(Request.get(path), lense)

  def self: Task[User]   = getPathAs[User]("/user")
  def places: Task[Json] = getPathAs[Json]("/places")

  private def logActivitiesCount(activities: Activities) =
    itemsCounter.incrementBy(activities.length.toLong)

  def activities(
    limit: Int = activitiesPageLimit,
    startIdExclusive: Option[Long] = None,
    startTime: Option[Long] = None,
    sinceTime: Option[Long] = None,
    mountId: Option[UUID] = None,
    categories: Set[String] = Set.empty
  ): Task[Activities] =
    getAs[Activities](
      Request
        .get("/user/activity")
        .setQueryParams(
          "limit"            -> Chunk(limit.toString),
          "startIdExclusive" -> Chunk.fromIterable(startIdExclusive.map(_.toString)),
          "startTime"        -> Chunk.fromIterable(startTime.map(_.toString)),
          "sinceTime"        -> Chunk.fromIterable(sinceTime.map(_.toString)),
          "mountId"          -> Chunk.fromIterable(mountId.map(_.toString)),
          "categories"       -> Chunk.fromIterable(Option.when(categories.nonEmpty)(categories.mkString(",")))
        ),
      _.hcursor.downField("activity").focus.getOrElse(Json.arr())
    ).tap(logActivitiesCount)

object KoofrClient:
  def fromClient(client: Client): KoofrClient = apply(client)

object KoofrService:
  private val apiVersion = "v2.1"
  private val apiUrl     = URL.decode("https://app.koofr.net").toOption.get / "api" / apiVersion

  def layer: ZLayer[WatchConfig & Client, Nothing, KoofrService] = ZLayer.scoped:
    for
      (username, password) <- ZIO.serviceWith[WatchConfig](c => c.koofrUsername -> c.koofrPassword)
      client               <-
        ZIO.serviceWith[Client](
          _.addUrl(apiUrl).addHeaders(
            Headers(
              Header.Authorization.Basic(username, password),
              Accept(MediaType.application.json),
              ContentType(MediaType.application.`json`)
            )
          ) @@ ZClientAspect.requestLogging(logResponseBody = true)
        )
    yield KoofrClient.fromClient(client)
