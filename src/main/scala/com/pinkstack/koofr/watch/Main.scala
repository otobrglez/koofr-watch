package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.*
import zio.*
import zio.http.*
import zio.metrics.Metric
import zio.metrics.connectors.{prometheus, MetricsConfig}
import zio.logging.backend.SLF4J
import zio.stream.ZStream
import eu.timepit.refined.auto.autoUnwrap
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.metrics.jvm.DefaultJvmMetrics

object MetricsServer:
  private val routes = Routes(
    Method.GET / "metrics" ->
      handler(
        ZIO.serviceWithZIO[PrometheusPublisher](_.get.map(Response.text))
      )
  )

  def serve =
    Server.serve(routes)

object Main extends ZIOAppDefault:
  private val metricsConfig = ZLayer.succeed(MetricsConfig(1.seconds))
  override val bootstrap    = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private def program = for
    koofr         <- ZIO.service[KoofrService]
    webhooks      <- ZIO.service[Webhooks]
    collectionFib <- ZStream.serviceWithStream[ActivitiesOf](_.stream).via(webhooks.pipeline()).runDrain.fork
    serverFib     <- MetricsServer.serve.fork
    _             <- collectionFib.join
  yield ()

  def run = program.provide(
    Scope.default,
    WatchConfig.layer,
    KoofrService.layer,
    ActivitiesOf.layer,
    Webhooks.layer,
    Client.default,
    WatchConfig.layer.flatMap(c =>
      ZLayer.fromZIO(
        ZIO.logInfo(s"Booting server monitoring on port ${c.get.port}")
      ) >>> Server.defaultWithPort(c.get.port)
    ),

    // Metrics
    metricsConfig,
    prometheus.publisherLayer,
    prometheus.prometheusLayer,
    Runtime.enableRuntimeMetrics,
    DefaultJvmMetrics.live.unit
  )
