package com.pinkstack.koofr.watch.koofr

import io.circe.{Decoder, Json}
import io.circe.generic.semiauto.deriveDecoder

import java.time.{Instant, ZoneId, ZoneOffset, ZonedDateTime}
import java.util.UUID
import scala.util.Try

enum ActivityType:
  case FileMoved
  case FileDeleted
  case FileUploaded
  case LinkCreated

object ActivityType:
  given activityTypeDecoder: Decoder[ActivityType] = Decoder.decodeString.emap:
    case "file_moved"    => Right(FileMoved)
    case "file_deleted"  => Right(FileDeleted)
    case "file_uploaded" => Right(FileUploaded)
    case "link_created"  => Right(LinkCreated)
    case other           => Left(s"Unknown activity type: $other")

final case class User(
  id: UUID,
  name: Option[String],
  email: String,
  firstName: Option[String],
  lastName: Option[String]
)
object User:
  given userDecoder: Decoder[User] = deriveDecoder[User]

object Koofr:
  import ActivityType.*

  type Timestamp = ZonedDateTime

  final case class Activity(
    `type`: ActivityType,
    id: String,
    timestamp: Timestamp,
    mountName: String,
    mountId: UUID,
    user_agent: String,
    newPath: Option[String],
    newMountId: Option[UUID],
    newMountName: Option[String],
    user: User,
    path: Option[String],
    linkId: Option[UUID]
  )

  type Activities = List[Activity]
  // type Activities = List[Json] // Use for debugging.

  given activityDecoder: Decoder[Activity] = deriveDecoder[Activity]

  given timestampDecoder: Decoder[Timestamp] =
    Decoder.decodeLong.emapTry(n => Try(Instant.ofEpochMilli(n).atZone(ZoneOffset.UTC)))
