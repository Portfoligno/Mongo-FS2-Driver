package io.github.portfoligno.fs2.mongo

trait Wrapped[A] extends Any {
  def underlying: A

  override
  def toString: String = underlying.toString
}
