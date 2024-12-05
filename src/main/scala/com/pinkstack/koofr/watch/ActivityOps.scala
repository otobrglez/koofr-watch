package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.ActivityType.{FileDeleted, FileMoved, FileUploaded}
import com.pinkstack.koofr.watch.koofr.Koofr.Activity
import com.pinkstack.koofr.watch.koofr.{ActivityType, User}

object ActivityOps:
  import ActivityType.*
  private def who(activity: Activity): String =
    activity.user.firstName
      .orElse(activity.user.lastName)
      .getOrElse(activity.user.name.getOrElse("Unknown"))

  extension (a: Activity)
    def print: String = a match
      case Activity(
            FileMoved,
            _,
            _,
            mountId,
            mountName,
            _,
            Some(newPath),
            newMountId,
            Some(newMountName),
            User(_, Some(name), _, _, _),
            Some(path),
            _,
            _,
            _
          ) if mountName == newMountName =>
        s"${who(a)} moved: ($mountName) '$path' to '$newPath'"
      case Activity(
            FileMoved,
            _,
            _,
            mountId,
            mountName,
            _,
            Some(newPath),
            newMountId,
            Some(newMountName),
            User(_, Some(name), _, _, _),
            Some(path),
            _,
            _,
            _
          ) =>
        s"${who(a)} moved: ($mountName) '$path' to ($newMountName) '$newPath'"
      case Activity(
            FileUploaded,
            _,
            _,
            mountId,
            mountName,
            _,
            _,
            _,
            _,
            User(_, Some(name), _, _, _),
            Some(path),
            _,
            _,
            _
          ) =>
        s"${who(a)} uploaded: ($mountName) '$path'"
      case Activity(
            FileDeleted,
            _,
            _,
            mountId,
            mountName,
            _,
            _,
            _,
            _,
            User(_, Some(name), _, _, _),
            Some(path),
            _,
            _,
            _
          ) =>
        s"${who(a)} deleted: ($mountName) '$path'"
      case a => s"Activity: $a"
