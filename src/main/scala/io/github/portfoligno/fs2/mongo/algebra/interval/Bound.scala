package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.instances.all._
import cats.syntax.eq._
import cats.{Eq, ~>}

sealed trait Bound[+O[_], +A] {
  val value: A

  def isClosed: Boolean
}


final case class Open[+O[_], +A](override val value: A) extends Bound[O, A] {
  override
  def isClosed: Boolean = false
}

final case class Closed[+O[_], +A](override val value: A) extends Bound[O, A] {
  override
  def isClosed: Boolean = true
}


object Bound {
  implicit def eqInstance[O[_], A](implicit ev: O ~> Eq, A : O[A]): Eq[Bound[O, A]] = {
    implicit val eq: Eq[A] = ev(A)

    (x, y) =>
      x.isClosed === y.isClosed && x.value === y.value
  }
}
