package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.instances.all._
import cats.{Eq, Order, ~>}

private[interval]
trait IntervalInstances {
  implicit def eqInstance[O[_], A : O : Eq](implicit ev: O ~> Order): Eq[Interval[O, A]] =
    Eq.by(Projection.unapply[O, A])
}
