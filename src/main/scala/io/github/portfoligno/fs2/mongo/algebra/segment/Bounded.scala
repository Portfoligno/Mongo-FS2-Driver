package io.github.portfoligno.fs2.mongo.algebra.segment

import cats.{Order, ~>}

/**
  * An extractor for the ordered bounds and direction based on the `O` context
  */
object Bounded {
  def unapply[O[_], A](
    segment: Segment[O, A]
  )(
    implicit O: O[A], ev: O ~> Order
  ): Option[(Option[Bound[A]], Option[Bound[A]], Int)] =
    segment match {
      case Valued(left @ Some(Bound(l, lc)), right @ Some(Bound(r, rc))) =>
        ev(O).compare(l, r) match {
          case 0 if lc && rc =>
            Some((left, right, 1)) // Pointed Interval

          case 0 =>
            Some((left, right, 0)) // Empty Interval

          case k if k < 0 =>
            Some((left, right, 1)) // Forward Bounded Interval

          case k if k > 0 =>
            Some((right, left, -1)) // Backward Bounded Interval
        }

      case Valued(left, right) =>
        Some((left, right, 1)) // Half-bounded Interval

      case Nil =>
        Some((None, None, 0)) // Empty interval
    }
}
