package com.pinkstack.koofr.watch.koofr

import com.pinkstack.koofr.watch.WatchConfig
import io.circe.parser.parse as jsonParse
import io.circe.{Decoder, Json}
import zio.*
import zio.http.*
import zio.http.Header.{Accept, ContentType}
import eu.timepit.refined.auto.autoUnwrap
import scala.util.control.NoStackTrace

enum KoofrError(message: String) extends Throwable(message) with NoStackTrace:
  case APIError(status: Status)   extends KoofrError(s"Koofr API error: $status")
  case JsonError(message: String) extends KoofrError(s"JSON parsing error: $message")

final case class KoofrClient private (
  private val client: Client
):
  import Koofr.*
  import KoofrError.*
  private val activitiesPageLimit: Int = 20

  private def loggingClient =
    client @@ ZClientAspect.requestLogging()

  private def handleResponse(response: Response): Task[Json] = for
    body <- response.body.asString
    _    <- ZIO.when(response.status.isError)(ZIO.fail(APIError(response.status)))
    json <- ZIO.fromEither(jsonParse(body)).mapError(pf => JsonError(pf.getMessage))
  yield json

  private def decodeResponseAs[A](response: Response, lense: Json => Json = identity)(using Decoder[A]): Task[A] =
    handleResponse(response).flatMap(json => ZIO.fromEither(lense(json).as[A]))

  private def getAs[A](request: Request, lense: Json => Json = identity)(using Decoder[A]): Task[A] =
    loggingClient.batched(request).flatMap(r => decodeResponseAs[A](r, lense))

  private def getPathAs[A](path: String, lense: Json => Json = identity)(using Decoder[A]): Task[A] =
    getAs(Request.get(path), lense)

  def self: Task[User]                                               = getPathAs[User]("/user")
  def places: Task[Json]                                             = getPathAs[Json]("/places")
  def activities(limit: Int = activitiesPageLimit): Task[Activities] =
    getAs[Activities](
      Request.get("/user/activity").setQueryParams("limit" -> Chunk(limit.toString)),
      _.hcursor.downField("activity").focus.getOrElse(Json.arr())
    )

object KoofrClient:
  private val apiVersion = "v2.1"
  private val apiUrl     = URL.decode("https://app.koofr.net").toOption.get / "api" / apiVersion

  def layer: ZLayer[WatchConfig & Client, Nothing, KoofrClient] = ZLayer.scoped:
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
          )
        )
    yield apply(client)
