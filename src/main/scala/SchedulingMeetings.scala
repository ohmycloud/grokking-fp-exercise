import cats.effect.IO
import cats.implicits.*
import cats.effect.unsafe.implicits.global

case class MeetingTime(startHour: Int, endHour: Int)
// Requirements: Meeting scheduler
// 1. For given two attendees and a meeting length, your function should be able
//    to find a common free slot.
// 2. Your function should persist the meeting in the given slot in all attendees’
//    calendars.
// 3. Your function should use impure functions that talk with the outside world:
//    calendarEntriesApiCall and createMeetingApiCall without modifying
//    them. (Assume they are provided by an external client library.

// An API may return diffrenet results for the same argument.
// An API call may fail with a connection(or another) error.
// An API call may take too long to finish.
object SchedulingMeetings extends App {
  // Scala can use imperative code
  // We can use Java functions in Scala,
  // since they are both JVM languages.
  def calendarEntriesApiCall(name: String): List[MeetingTime] = {
    import scala.jdk.CollectionConverters._
    SchedulingMeetingsAPI.calendarEntriesApiCall(name).asScala.toList
  }

  def createMeetingApiCall(
                            names: List[String],
                            meetingTime: MeetingTime
                          ): Unit = {
    import scala.jdk.CollectionConverters._
    SchedulingMeetingsAPI.createMeetingApiCall(names.asJava, meetingTime)
  }

  // IO[A] is a value that represents a potentially side-effect IO action

  def calendarEntries(name: String): IO[List[MeetingTime]] = {
    IO.delay(calendarEntriesApiCall(name))
  }

  def createMeeting(names: List[String],
                    meeting: MeetingTime
                   ): IO[Unit] = {
    IO.delay(createMeetingApiCall(List("Alice", "Bob"), MeetingTime(11, 12)))
  }
  def scheduledMeetings(person1: String, person2: String): IO[List[MeetingTime]] = {
    for {
      meetings1 <- calendarEntries(person1)
      meetings2 <- calendarEntries(person2)
    } yield meetings1.appendedAll(meetings2)
  }

  scheduledMeetings("Alice", "Bob")
}
