package com.pinkstack.koofr.watch

import eu.timepit.refined.api.*
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.*
import eu.timepit.refined.types.string.NonEmptyString
import zio.Config.{int, Error}
import zio.config.*
import zio.config.refined.*
import zio.http.URL
import zio.prelude.NonEmptyList
import zio.{Config, ConfigProvider, IO, TaskLayer, ZLayer}
import eu.timepit.refined.string.Url
import zio.Config.Error.InvalidData

type KoofrUsername = NonEmptyString
type KoofrPassword = NonEmptyString

final case class Webhook(url: URL)

final case class WatchConfig private (
  port: Refined[Int, Positive],
  koofrUsername: KoofrUsername,
  koofrPassword: KoofrPassword,
  webhooks: NonEmptyList[URL]
)

object WatchConfig:
  import zio.config.magnolia.*

  private val listOfWebhooksConfig: List[Config[Option[URL]]] =
    (1 to 5)
      .map(n => s"WEBHOOK_URL_$n")
      .map(n =>
        refine[String, Url](n).optional.mapOrFail {
          case Some(url) =>
            URL
              .decode(url.value)
              .left
              .map(ex => InvalidData(message = s"Invalid URL at $n: ${ex.getMessage}"))
              .map(Some(_))
          case None      => Right(None)
        }
      )
      .toList

  private val listOfUrls: Config[List[Option[URL]]] =
    Config.collectAll(listOfWebhooksConfig.head, listOfWebhooksConfig.tail*)

  private val listOfUrlsToNonEmpty: List[Option[URL]] => Either[Config.Error, NonEmptyList[URL]] =
    list =>
      NonEmptyList
        .fromIterableOption(list.collect { case Some(url) => url })
        .fold(Left(InvalidData(message = "You need to set at least one Webhook URL")))(Right(_))

  private def configuration: Config[WatchConfig] = (
    refine[Positive](int("PORT")) zip
      refine[String, NonEmpty]("KOOFR_USERNAME") zip
      refine[String, NonEmpty]("KOOFR_PASSWORD") zip
      listOfUrls.mapOrFail(listOfUrlsToNonEmpty)
  ).to[WatchConfig]

  private def fromEnvironment: IO[Error, WatchConfig] =
    read(configuration.from(ConfigProvider.envProvider))

  def layer: TaskLayer[WatchConfig] = ZLayer.fromZIO(fromEnvironment)
