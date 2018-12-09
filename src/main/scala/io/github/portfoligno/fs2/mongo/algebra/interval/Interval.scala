package io.github.portfoligno.fs2.mongo.algebra.interval

final case class Interval[O[_], A](
  lowerBound: Bound[A],
  upperBound: Bound[A]
)
