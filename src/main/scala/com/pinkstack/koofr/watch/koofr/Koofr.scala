package com.pinkstack.koofr.watch.koofr

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import java.time.{Instant, ZoneOffset, ZonedDateTime}
import java.util.UUID
import scala.util.Try

enum ActivityType(
  val kind: String,
  val template: String
):
  case FileCopied
      extends ActivityType("file_copied", "{{user}} copied {{path}} from {{mount}} to {{newPath}} in {{newMount}}.")
  case FileDeleted    extends ActivityType("file_deleted", "{{user}} removed {{path}} from {{mount}}.")
  case FileDirCreated extends ActivityType("file_dir_created", "{{user}} created a folder {{path}} in {{mount}}.")
  case FileMoved
      extends ActivityType("file_moved", "{{user}} moved {{path}} from {{mount}} to {{newPath}} in {{newMount}}.")
  case FileRenamed    extends ActivityType("file_renamed", "{{user}} renamed {{path}} in {{mount}} to {{newPath}}.")
  case FileUploaded   extends ActivityType("file_uploaded", "{{user}} uploaded {{path}} to {{mount}}.")
  case LinkCreated    extends ActivityType("link_created", "{{user}} created {{linkStart}}a download link{{linkEnd}}.")
  case MountUserAdded extends ActivityType("mount_user_added", "{{user}} added user {{mountUser}} to {{mount}}.")
  case MountUserRemoved
      extends ActivityType("mount_user_removed", "{{user}} removed user {{mountUser}} from {{mount}}.")

object ActivityType:
  given activityTypeDecoder: Decoder[ActivityType] = Decoder.decodeString.emap:
    case "file_copied"        => Right(FileCopied)
    case "file_deleted"       => Right(FileDeleted)
    case "file_dir_created"   => Right(FileDirCreated)
    case "file_moved"         => Right(FileMoved)
    case "file_renamed"       => Right(FileRenamed)
    case "file_uploaded"      => Right(FileUploaded)
    case "link_created"       => Right(LinkCreated)
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

  given activityDecoder: Decoder[Activity] = deriveDecoder[Activity]

  given timestampDecoder: Decoder[Timestamp] =
    Decoder.decodeLong.emapTry(n => Try(Instant.ofEpochMilli(n).atZone(ZoneOffset.UTC)))
