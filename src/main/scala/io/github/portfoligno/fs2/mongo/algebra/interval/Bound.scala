package io.github.portfoligno.fs2.mongo.algebra.interval

sealed trait Bound[+A] extends Any

case object UnBounded extends Bound[Nothing]


sealed trait Bounded[A] extends Any with Bound[A] {
  def value: A
}

object Bounded {
  def unapply[A](bound: Bounded[A]): Option[A] = bound match {
    case Open(value) =>
      Some(value)

    case Closed(value) =>
      Some(value)
  }
}

final case class Open[A](
  override
  val value: A
)
  extends AnyVal with Bounded[A]

final case class Closed[A](
  override
  val value: A
)
  extends AnyVal with Bounded[A]
