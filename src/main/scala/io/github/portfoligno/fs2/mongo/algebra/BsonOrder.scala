package io.github.portfoligno.fs2.mongo.algebra

import cats.Order

trait BsonOrder[A] extends Any {
  def toOrder: Order[A]
}

object BsonOrder extends BsonOrderInstances {
  private[algebra]
  case class FromOrder[A](override val toOrder: Order[A]) extends AnyVal with BsonOrder[A]


  def fromOrder[A](implicit order: Order[A]): BsonOrder[A] = FromOrder(order)

  def fromComparable[A <: Comparable[A]]: BsonOrder[A] = FromOrder(Order.fromComparable)
}
