package io.github.portfoligno.fs2.mongo.algebra.interval

import scala.math.signum

private[interval]
class PartiallyApplied[O[_]](private val dummy: Boolean = true) extends AnyVal {
  def apply[A](start: Option[Bound[A]], end: Option[Bound[A]], direction: Double): Interval[O, A] =
    signum(direction) match {
      case 0 =>
        Interval.empty

      case 1 =>
        Product(start, end).proper

      case -1 =>
        Product(end, start).proper.dual

      case _ =>
        Product(start, end)
    }
}

private[interval]
trait IntervalInstances {
  def empty[O[_], A]: Interval[O, A] =
    Empty.asInstanceOf[Interval[O, A]]

  def apply[O[_]]: PartiallyApplied[O] =
    new PartiallyApplied[O]
}
