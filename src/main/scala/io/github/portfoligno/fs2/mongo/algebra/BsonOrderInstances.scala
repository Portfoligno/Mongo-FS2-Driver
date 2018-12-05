package io.github.portfoligno.fs2.mongo.algebra

import java.util.Date

import cats.Order
import cats.instances.all._
import org.bson.types.BSONTimestamp

private[algebra]
trait BsonOrderInstances {
  def fromOrder[A](implicit order: Order[A]): BsonOrder[A] = FromOrder(order)

  def fromComparable[A <: Comparable[A]]: BsonOrder[A] = FromOrder(Order.fromComparable)


  /**
    * @see [[org.bson.io.OutputBuffer#writeCharacters]]
    */
  implicit lazy val stringBsonOrder: BsonOrder[String] = fromOrder

  /**
    * @see [[org.bson.BsonBinaryWriter#doWriteBoolean]]
    */
  implicit lazy val booleanBsonOrder: BsonOrder[Boolean] = fromOrder

  /**
    * @see https://docs.mongodb.com/manual/reference/bson-types/#date
    */
  implicit lazy val dateBsonOrder: BsonOrder[Date] = fromComparable

  /**
    * @see https://docs.mongodb.com/manual/reference/bson-types/#timestamps
    */
  implicit lazy val bsonTimestampBsonOrder: BsonOrder[BSONTimestamp] = fromComparable
}
