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
  case FileRenamed
  case LinkCreated
  case MountUserAdded
  case MountUserRemoved
  case FileDirCreated

object ActivityType:
  given activityTypeDecoder: Decoder[ActivityType] = Decoder.decodeString.emap:
    case "file_moved"         => Right(FileMoved)
    case "file_deleted"       => Right(FileDeleted)
    case "file_uploaded"      => Right(FileUploaded)
    case "link_created"       => Right(LinkCreated)
    case "file_renamed"       => Right(FileRenamed)
    case "file_dir_created"   => Right(FileDirCreated)
    case "mount_user_added"   => Right(MountUserAdded)
    case "mount_user_removed" => Right(MountUserRemoved)
    case other                => Left(s"Unknown activity type: $other")

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
    mountId: UUID,
    mountName: String,
    user_agent: String,
    newPath: Option[String],
    newMountId: Option[UUID],
    newMountName: Option[String],
    user: User,
    path: Option[String],
    linkId: Option[UUID],
    mountUserId: Option[UUID],
    mountUserName: Option[String]
  ):
    override def equals(obj: Any): Boolean = obj match
      case that: Activity => this.id == that.id
      case _              => false

    override def hashCode(): Int = id.hashCode

  type Activities = List[Activity]
  // type Activities = List[Json] // Use for debugging.

  given activityDecoder: Decoder[Activity] = deriveDecoder[Activity]

  given timestampDecoder: Decoder[Timestamp] =
    Decoder.decodeLong.emapTry(n => Try(Instant.ofEpochMilli(n).atZone(ZoneOffset.UTC)))
