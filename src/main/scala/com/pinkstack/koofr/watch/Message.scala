package com.pinkstack.koofr.watch

import com.pinkstack.koofr.watch.koofr.{ActivityType, User}
import com.pinkstack.koofr.watch.koofr.Koofr.Activity
import zio.Chunk

type Message = String
object Message:
  import ActivityOps.*

  private val who: Activity => String =
    case activity @ Activity(
          _,
          _,
          _,
          _,
          _,
          _,
          _,
          _,
          _,
          user @ User(id, name, email, firstName, lastName),
          _,
          _,
          _,
          _
        ) =>
      firstName.orElse(lastName).getOrElse(name.getOrElse("Unknown"))

  def fromActivities(activities: Chunk[Activity]): Message =
    activities
      .groupBy(_.`type`)
      .map((t, a) => t -> a.sortBy(_.`type`.ordinal))
      .map { case (activityType, activities) =>
        activities
          .map(a => s"- ${a.render}")
          .mkString("\n")
      }
      .mkString("\n")

  def fromActivity(activity: Activity*): Message =
    fromActivities(Chunk.fromIterable(activity))
