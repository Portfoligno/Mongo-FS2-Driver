package io.github.portfoligno.fs2.mongo.algebra.interval

import scala.math.signum

private[interval]
trait IntervalInstances {
  def zero[O[_], A]: Interval[O, A] =
    Zero.asInstanceOf[Interval[O, A]]

  def apply[O[_], A](start: Option[Bound[A]], end: Option[Bound[A]], direction: Double): Interval[O, A] =
    signum(direction) match {
      case 0 =>
        zero

      case 1 =>
        Product(start, end).proper

      case -1 =>
        Product(start, end).dual.proper

      case _ =>
        Product(start, end)
    }
}
