package io.github.portfoligno.fs2.mongo.algebra

import cats.Order
import cats.instances.all._

private[algebra]
trait BsonOrderInstances {
  def fromOrder[A](implicit order: Order[A]): BsonOrder[A] = FromOrder(order)

  def fromComparable[A <: Comparable[A]]: BsonOrder[A] = FromOrder(Order.fromComparable)

  implicit lazy val booleanBsonOrder: BsonOrder[Boolean] = fromOrder
}
