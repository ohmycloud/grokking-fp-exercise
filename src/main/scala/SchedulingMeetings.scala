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
  // This function returns a description of a side-effectful IO action that,
  // when executed, will return a list of MeetingTimes.
  def calendarEntries(name: String): IO[List[MeetingTime]] = {
    IO.delay(calendarEntriesApiCall(name))
  }

  // This function returns a description of a side-effectful IO action that,
  // when executed, will not return anything(hence IO[Unit]).
  def createMeeting(names: List[String],
                    meeting: MeetingTime
                   ): IO[Unit] = {
    IO.delay(createMeetingApiCall(List("Alice", "Bob"), MeetingTime(11, 12)))
  }

  // check whether two meeting times overlap.
  def meetingsOverlap(meeting1: MeetingTime, meeting2: MeetingTime): Boolean = {
    meeting1.endHour > meeting2.startHour &&
    meeting2.endHour > meeting1.startHour
  }

  def possibleMeetings(
    existingMeetings: List[MeetingTime],
    startHour: Int,
    endHour: Int,
    lengthHours: Int
  ): List[MeetingTime] = {
    val slots = List
      .range(startHour, endHour - lengthHours + 1)
      .map(startHour => MeetingTime(startHour, startHour + lengthHours))
    slots.filter(slot =>
      existingMeetings.forall(meeting => !meetingsOverlap(meeting, slot))
    )
  }

  // This function returns a description of a side-effectful IO action that,
  // when executed, will return a list of MeetingTimes for two attendees.
  def scheduledMeetings(person1: String, person2: String): IO[List[MeetingTime]] = {
    for {
      meetings1 <- calendarEntries(person1)
      meetings2 <- calendarEntries(person2)
    } yield meetings1.appendedAll(meetings2)
  }

  // This function returns a description of a side-effectful IO action that, when executed,
  // will return a possible MeetingTime. It is build using smaller descriptions(IO values),
  // but no action is executed in this function. It's all just a combination of descriptions.
  def schedule(person1: String, person2: String, lengthHours: Int): IO[Option[MeetingTime]] = {
    for {
      existingMeetings <- scheduledMeetings(person1, person2)
      meetings = possibleMeetings(existingMeetings, 8, 16, lengthHours)
    } yield meetings.headOption
  }

  println(schedule("Alice", "Bob", 2).unsafeRunSync())
}
