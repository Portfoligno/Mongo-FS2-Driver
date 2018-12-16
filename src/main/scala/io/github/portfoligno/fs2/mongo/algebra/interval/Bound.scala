package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.Eq
import cats.instances.all._
import cats.syntax.eq._

sealed trait Bound[+A] {
  val value: A

  def isClosed: Boolean
}


final case class Open[+A](override val value: A) extends Bound[A] {
  override
  def isClosed: Boolean = false
}

final case class Closed[+A](override val value: A) extends Bound[A] {
  override
  def isClosed: Boolean = true
}

object Bound {
  implicit def eqInstance[A : Eq]: Eq[Bound[A]] =
    (x, y) =>
      x.isClosed === y.isClosed && x.value === y.value
}
