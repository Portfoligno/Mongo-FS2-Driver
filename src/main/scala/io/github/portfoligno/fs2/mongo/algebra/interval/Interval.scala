package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.{Order, ~>}
import fs2.Pure

sealed trait Interval[O[_], A]

case object Nil extends Interval[Pure, Nothing]

final case class Valued[O[_], A](lowerBound: Option[Bound[A]], upperBound: Option[Bound[A]]) extends Interval[O, A]

object Interval {
  private[interval]
  class ClosedPartiallyApplied[O[_]](val dummy: Boolean = true) extends AnyVal {
    def apply[A](lower: A, upper: A)(implicit ev: O ~> Order): Interval[O, A] =
      Valued(Some(Closed(lower)), Some(Closed(upper)))
  }

  def empty[O[_], A](implicit ev: O ~> Order): Interval[O, A] = Nil.asInstanceOf[Interval[O, A]]

  def closed[O[_]]: ClosedPartiallyApplied[O] = new ClosedPartiallyApplied[O]
}
