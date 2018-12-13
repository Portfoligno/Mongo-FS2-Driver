package io.github.portfoligno.fs2.mongo.algebra.interval

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
