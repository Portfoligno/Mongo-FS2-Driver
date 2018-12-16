package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.instances.all._
import cats.{Eq, Order, ~>}

private[interval]
trait IntervalInstances {
  implicit def eqInstance[O[_], A](implicit ev: O ~> Order, A : O[A]): Eq[Interval[O, A]] = {
    implicit val ev1: O ~> Eq = ev.asInstanceOf[O ~> Eq]

    Eq.by(Projection.unapply[O, A])
  }
}
