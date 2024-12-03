package com.pinkstack.koofr.watch

import eu.timepit.refined.api.*
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.numeric.*
import eu.timepit.refined.types.string.NonEmptyString
import zio.Config.{int, Error}
import zio.config.*
import zio.config.refined.*
import zio.{Config, ConfigProvider, IO, TaskLayer, ZLayer}

type KoofrUsername = NonEmptyString
type KoofrPassword = NonEmptyString

final case class WatchConfig private (
  port: Refined[Int, Positive],
  koofrUsername: KoofrUsername,
  koofrPassword: KoofrPassword
)

object WatchConfig:
  import zio.config.magnolia.*

  private def configuration: Config[WatchConfig] = (
    refine[Positive](int("PORT")) zip
      refine[String, NonEmpty]("KOOFR_USERNAME") zip
      refine[String, NonEmpty]("KOOFR_PASSWORD")
  ).to[WatchConfig]

  def fromEnvironment: IO[Error, WatchConfig] =
    read(configuration.from(ConfigProvider.envProvider))

  def layer: TaskLayer[WatchConfig] = ZLayer.fromZIO(fromEnvironment)
