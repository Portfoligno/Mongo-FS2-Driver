package io.github.portfoligno.fs2.mongo.algebra.interval

sealed case class Bound[A](value: A, isClosed: Boolean)

final case class Open[A](override val value: A) extends Bound[A](value, false)

final case class Closed[A](override val value: A) extends Bound[A](value, true)
