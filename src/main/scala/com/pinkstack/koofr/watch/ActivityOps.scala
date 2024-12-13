package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.ActivityType.{FileDeleted, FileMoved, FileUploaded}
import com.pinkstack.koofr.watch.koofr.Koofr.Activity
import com.pinkstack.koofr.watch.koofr.{ActivityType, User}
import zio.prelude.data.Optional.AllValuesAreNullable

object ActivityOps:
  import ActivityType.*
  private def who(activity: Activity): String =
    activity.user.firstName
      .orElse(activity.user.lastName)
      .getOrElse(activity.user.name.getOrElse("Unknown"))

  // TODO: Missing file_copied

  private val unknown: String = "'Unknown'"

  extension (a: Activity)
    def render: String =
      val template = a.`type`.template
      template
        .replace("{{user}}", who(a))
        .replace("{{path}}", a.path.getOrElse(unknown))
        .replace("{{mount}}", a.mountName.getOrElse(unknown))
        .replace("{{newMount}}", a.newMountName.getOrElse(unknown))
        .replace("{{newPath}}", a.newPath.getOrElse(unknown))
        .replace("{{mountUser}}", a.mountUserName.getOrElse(unknown))
        .replace("{{linkStart}}", "")
        .replace("{{linkEnd}}", "")

// s"${who(a)} ${a.`type`} ${a.path}"
