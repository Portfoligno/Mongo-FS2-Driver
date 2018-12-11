package io.github.portfoligno.fs2.mongo.algebra.segment

import cats.{Order, ~>}
import fs2.Pure

sealed trait Segment[O[_], A] extends Any

case object Nil extends Segment[Pure, Nothing]

final case class Valued[O[_], A](lowerBound: Option[Bound[A]], upperBound: Option[Bound[A]]) extends Segment[O, A]


object Segment {
  private[segment]
  class ClosedPartiallyApplied[O[_]](val dummy: Boolean = true) extends AnyVal {
    def apply[A](lower: A, upper: A)(implicit ev: O ~> Order): Segment[O, A] =
      Valued(Some(Closed(lower)), Some(Closed(upper)))
  }

  def empty[O[_], A](implicit ev: O ~> Order): Segment[O, A] = Nil.asInstanceOf[Segment[O, A]]

  def closed[O[_]]: ClosedPartiallyApplied[O] = new ClosedPartiallyApplied[O]
}
