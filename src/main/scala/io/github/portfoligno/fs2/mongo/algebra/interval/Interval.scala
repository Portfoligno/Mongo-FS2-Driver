package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.{Order, ~>}
import fs2.Pure

sealed trait Interval[O[_], A]

final case class BasicInterval[O[_], A](
  lowerBound: Bound[A],
  upperBound: Bound[A]
)
  extends Interval[O, A]

object Interval {
  case object Nil extends Interval[Pure, Nothing]

  /**
    * An extractor to perform emptiness check based on the `O` context
    */
  object Empty {
    def unapply[O[_], A](arg: Interval[O, A])(implicit O: O[A], ev: O ~> Order): Boolean =
      arg match {
        case Nil =>
          true

        case BasicInterval(Open(lower), Open(upper)) if ev(O).compare(lower, upper) >= 0 =>
          true

        case BasicInterval(Bounded(lower), Bounded(upper)) if ev(O).compare(lower, upper) > 0 =>
          true

        case _ =>
          false
      }
  }

  private[interval]
  class ClosedPartiallyApplied[O[_]](val dummy: Boolean = true) extends AnyVal {
    def apply[A](lower: A, upper: A)(implicit ev: O ~> Order): Interval[O, A] =
      BasicInterval(Closed(lower), Closed(upper))
  }


  def empty[O[_], A](implicit ev: O ~> Order): Interval[O, A] = Nil.asInstanceOf[Interval[O, A]]

  def closed[O[_]]: ClosedPartiallyApplied[O] = new ClosedPartiallyApplied[O]
}
