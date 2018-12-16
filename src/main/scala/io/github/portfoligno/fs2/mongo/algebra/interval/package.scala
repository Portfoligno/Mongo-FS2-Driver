package io.github.portfoligno.fs2.mongo.algebra

import fs2.Pure

import scala.math.signum

package object interval {
  sealed trait Interval[O[_], A] extends Any {
    def proper: Interval[O, A] = Proper(this)

    def dual: Interval[O, A] = Dual(this)
  }

  case object Empty extends Interval[Pure, Nothing] {
    override
    def proper: Interval[Pure, Nothing] = this

    override
    def dual: Interval[Pure, Nothing] = this
  }

  final case class Product[O[_], A](start: Option[Bound[A]], end: Option[Bound[A]]) extends Interval[O, A]


  final case class Proper[O[_], A](value: Interval[O, A]) extends AnyVal with Interval[O, A] {
    override
    def proper: Interval[O, A] = this
  }

  final case class Dual[O[_], A](value: Interval[O, A]) extends AnyVal with Interval[O, A] {
    override
    def dual: Interval[O, A] = value
  }


  object Interval extends IntervalInstances {
    private[interval]
    class PartiallyApplied[O[_]](private val dummy: Boolean = true) extends AnyVal {
      def apply[A](start: Option[Bound[A]], end: Option[Bound[A]], direction: Double): Interval[O, A] =
        signum(direction) match {
          case 0 =>
            empty

          case 1 =>
            Product(start, end).proper

          case -1 =>
            Product(end, start).proper.dual

          case _ =>
            Product(start, end)
        }
    }

    def empty[O[_], A]: Interval[O, A] =
      Empty.asInstanceOf[Interval[O, A]]

    def apply[O[_]]: PartiallyApplied[O] =
      new PartiallyApplied[O]
  }
}
