import fs2.Stream
import fs2.Pure
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global
import CastTheDieImpure.castTheDieImpure

object StreamExercise extends App {
  def castTheDie(): IO[Int] = IO.delay(castTheDieImpure())

  // dieCast is a Stream of a single IO value. eval takes an IO value and "evaluates" it(i.e, executes the given IO value)
  // and emits the Int produced by this IO action but only when a consumer requests it later on!
  // Stream.eval creates a new Stream value that, when used by a consumer, will produce one Int value, based on the result
  // of a side-effect program described by an IO value passed as the single parameter.
  val dieCast: Stream[IO, Int] = Stream.eval(castTheDie())

  // By calling .compile.toList on the dieCast stream, we transform it to another value: IO[List[Int]]
  val oneDieCastProgram: IO[List[Int]] = dieCast.compile.toList
//  println(oneDieCastProgram.unsafeRunSync())

  // repeat works for IO-based streams as well
  val infiniteDieCasts: Stream[IO, Int] = Stream.eval(castTheDie()).repeat
  val firstThreeCats: IO[List[Int]] = infiniteDieCasts.take(3).compile.toList
//  val infiniteDieCastsProgram: IO[Unit] = infiniteDieCasts.compile.drain
//  infiniteDieCastsProgram.unsafeRunSync()
//  val six: IO[List[Int]] = infiniteDieCasts.filter(_ == 6).take(1).compile.toList
//  println(six.unsafeRunSync())

  // There are lots of other cases where we may be interested only in side-effects: use interfaces, handing server endpoints
  // and socket connections

  // 1. Filter odd number, and return the first three such casts.
  val f1: IO[List[Int]] = infiniteDieCasts.filter(_ % 2 == 0).take(3).compile.toList
//  println(oddNumbers.unsafeRunSync())

  // 2. Return the first five die casts, but make all 6 values are bounded(so a [1,2,3,6,4] becomes [1, 2, 3, 12, 4])
  val f2: IO[List[Int]] = infiniteDieCasts.map(x => if (x == 6) {2* x} else {x} ).take(5).compile.toList

  // 3. return the sum of the first three casts.
  val f3 = infiniteDieCasts.take(3).compile.toList.map(_.sum)

//  println(sumOfFirstThree.unsafeRunSync())

  // 4. Cast the die until there is a 5 and then cast it two nore times,
  // returning three last results back(a 5 and two more)
  val f4: IO[List[Int]] = infiniteDieCasts.filter(_ == 5).take(1).append(infiniteDieCasts.take(2)).compile.toList

  // 5. Make sure the die is cast 100 times, and values are discarded.
  val f5 = infiniteDieCasts.take(100).compile.drain

  // 6. Return the first three casts unchanged and the next three casts tripled(six in total)
  val f6 = infiniteDieCasts.take(3).append(infiniteDieCasts.take(3).map(_ * 3)).compile.toList

  // 7. Cast the die until there are two 6s in a row
}
