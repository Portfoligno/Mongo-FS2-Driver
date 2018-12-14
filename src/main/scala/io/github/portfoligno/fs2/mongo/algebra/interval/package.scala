package io.github.portfoligno.fs2.mongo.algebra

import fs2.Pure

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


  object Interval extends IntervalInstances
}
