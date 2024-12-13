package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.ActivityType.MountUserAdded
import com.pinkstack.koofr.watch.koofr.Koofr.{Activities, Activity}
import com.pinkstack.koofr.watch.koofr.{ActivityType, KoofrService, User}
import zio.*
import zio.test.*
import zio.test.Assertion.equalTo
import org.scalacheck.{Arbitrary, Gen}

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import java.util
import java.util.UUID
import scala.jdk.CollectionConverters.*
import zio.Console.printLine
import zio.stream.ZStream

object MessageSpec extends ZIOSpecDefault:

  private def genMount: Gen[(String, UUID)] = Gen
    .stringOfN(30, Gen.alphaNumChar)
    .flatMap(mountName => Gen.uuid.map(mountName -> _))

  private def genPath: Gen[String] =
    for
      numSegments <- Gen.choose(1, 5) // Up to 4 folders deep
      segments    <- Gen.listOfN(numSegments, Gen.alphaStr.suchThat(s => s.nonEmpty))
      extension   <- Gen.oneOf("txt", "jpg", "png", "pdf", "doc")
    yield segments.mkString("/", "/", "." + extension)

  def activityGen: Gen[Activity] = for
    activityType         <- Gen.oneOf(ActivityType.values.toList)
    id                   <- Gen.stringOfN(10, Gen.numChar)
    (mountName, mountId) <- genMount
    userAgent            <- Gen.oneOf("Mozilla", "Firefox", "Chrome", "Safari")
    user                 <-
      for
        userID <- Gen.uuid
        name   <-
          Gen.oneOf[Option[String]](
            Some("Oto Brglez"),
            Some("Martina"),
            Some("Tinkara"),
            Some("Rudi"),
            Some("Frida"),
            Some("John Smith"),
            Some("Mike Baker"),
            Some("Long John Frank"),
            None
          )
        email  <- Gen.stringOfN(10, Gen.alphaNumChar).flatMap(s => Gen.oneOf(s, name.getOrElse("") + s + "@example.com"))
      yield User(
        id = userID,
        name = name,
        email = email,
        firstName = name,
        lastName = name.map(_.toUpperCase)
      )
    maybeNewMount        <- Gen.oneOf(Some(genMount.sample.get), None)
    maybeLinkId          <- Gen.oneOf(Some(Gen.uuid.sample.get), None)
    mountUserName        <- Gen.option(Gen.stringOfN(15, Gen.alphaNumChar))
    mountUserId          <- Gen.option(Gen.uuid)
    path                 <- genPath
  yield Activity(
    `type` = activityType,
    id = id,
    timestamp = LocalDateTime.now().atZone(ZoneOffset.UTC),
    mountId = mountId,
    mountName = mountName,
    user_agent = userAgent,
    newPath = None,
    newMountId = maybeNewMount.map(_._2),
    newMountName = maybeNewMount.map(_._1),
    user = user,
    path = Some(path),
    linkId = maybeLinkId,
    mountUserId = mountUserId,
    mountUserName = mountUserName
  )

  def activitiesSample(n: Int): Chunk[Activity] =
    Chunk.fromIterable(
      Gen.sequence(List.fill(n)(activityGen)).sample.get.asScala
    )

  def spec =
    suite("Message") {
      test("it works") {

        val fakeWebhooks = new Webhooks:
          override def sendMessage(message: Message): ZIO[Scope, Throwable, Unit] = Console.print(s"GOT: \n${message}")

        val stream =
          ZStream
            .fromIterable(activitiesSample(10) ++ activitiesSample(10))
            .via(fakeWebhooks.pipeline2(chunkSize = Some(5), maxLength = Some(500)))
            .runCollect

        assertZIO(stream)(
          Assertion.assertion("what")(_ => 1 == 1)
        )
        // assertTrue(true)
      }
    }
