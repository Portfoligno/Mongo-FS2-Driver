package io.github.portfoligno.fs2.mongo.algebra.segment

final case class SegmentT[F[_], O[_], A](
  value: F[Segment[O, A]]
)
