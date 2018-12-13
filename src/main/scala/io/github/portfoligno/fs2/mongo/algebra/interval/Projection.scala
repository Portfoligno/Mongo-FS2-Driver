package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.{Order, ~>}

import scala.math.signum

/**
  * An extractor for the bounds in order and the direction based on the `O` context
  */
object Projection {
  private
  type OB[A] = Option[Bound[A]]

  def unapply[O[_], A](segment: Interval[O, A])(
    implicit A: O[A], ev: O ~> Order
  ): Option[(Option[Bound[A]], Option[Bound[A]], Int)] =
    segment match {
      case Dual(Projection(left: OB[A], right: OB[A], direction)) =>
        Some((left, right, -direction))

      case Proper(Projection(left: OB[A], right: OB[A], direction)) =>
        if (direction > 0) {
          Some((left, right, direction))
        } else {
          Some((None, None, 0))
        }

      case Product(left @ Some(l), right @ Some(r)) =>
        signum(ev(A).compare(l.value, r.value)) match {
          case 0 if l.isClosed && r.isClosed =>
            Some((left, right, 1)) // Pointed Interval

          case 0 =>
            Some((None, None, 0)) // Empty Interval

          case -1 =>
            Some((left, right, 1)) // Proper Interval

          case 1 =>
            Some((right, left, -1)) // Improper Interval
        }

      case Product(left, right) =>
        Some((left, right, 1)) // (Half) Unbounded Interval

      case Zero =>
        Some((None, None, 0)) // Empty interval
    }
}
