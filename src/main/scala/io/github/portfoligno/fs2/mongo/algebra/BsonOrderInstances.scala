package io.github.portfoligno.fs2.mongo.algebra

import cats.Order
import cats.instances.all._
import org.bson.types.Binary

private[algebra]
trait BsonOrderInstances {
  def fromOrder[A](implicit order: Order[A]): BsonOrder[A] = FromOrder(order)

  def fromComparable[A <: Comparable[A]]: BsonOrder[A] = FromOrder(Order.fromComparable)


  implicit lazy val binaryOrder: BsonOrder[Binary] = ???

  implicit lazy val booleanOrder: BsonOrder[Boolean] = fromOrder
}
