package io.github.portfoligno.fs2.mongo.algebra.segment

sealed abstract class Bound[+A](val value: A, val isClosed: Boolean)

object Bound {
  def unapply[A](bound: Bound[A]): Option[(A, Boolean)] =
    Some((bound.value, bound.isClosed))
}


final case class Open[+A](override val value: A) extends Bound[A](value, false)

final case class Closed[+A](override val value: A) extends Bound[A](value, true)
