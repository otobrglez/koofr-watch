package com.pinkstack.koofr.watch
import com.pinkstack.koofr.watch.koofr.Koofr.{Activities, Activity}
import com.pinkstack.koofr.watch.koofr.KoofrService
import zio.*
import zio.test.*
import zio.test.Assertion.equalTo

import java.util.UUID

object KoofrSpec extends ZIOSpecDefault:
  def spec =
    suite("KoofrSpec") {
      test("it works") {
        val fakeClient = new KoofrService:
          override def activities(
            limit: Int,
            startIdExclusive: Option[Long],
            startTime: Option[Long],
            sinceTime: Option[Long],
            mountId: Option[UUID],
            categories: Set[String]
          ): Task[Activities] = ZIO.succeed(List.empty[Activity])

        

        println("Hello...")
        assertTrue(true)
      }
    }
