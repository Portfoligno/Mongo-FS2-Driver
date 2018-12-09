package io.github.portfoligno.fs2.mongo.algebra.interval

final case class IntervalT[F[_], O[_], A](
  value: F[Interval[O, A]]
)
