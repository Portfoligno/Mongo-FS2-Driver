package io.github.portfoligno.fs2.mongo.algebra.interval

sealed trait Bound[+A]


case object UnBounded extends Bound[Nothing]

final case class Open[A](a: A) extends Bound[A]

final case class Closed[A](a: A) extends Bound[A]
