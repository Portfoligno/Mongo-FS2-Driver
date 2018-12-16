package io.github.portfoligno.fs2.mongo.instance

import java.util.Date

import cats.Order
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder._
import spire.std.any._

trait StdInstances {
  /**
    * @see [[org.bson.BsonBinaryWriter#doWriteBoolean]]
    */
  implicit lazy val booleanBsonOrder: BsonOrder[Boolean] = fromOrder

  /**
    * @see https://docs.mongodb.com/manual/reference/bson-types/#date
    */
  implicit lazy val dateBsonOrder: BsonOrder[Date] = fromComparable

  /**
    * NaN < -Infinity < Finite numbers < +Infinity
    *
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    */
  implicit lazy val doubleBsonOrder: BsonOrder[Double] = fromOrder(
    Order.by[Double, Option[Double]](
      _.nonNaN
    )(
      noneFirst[Double]
    )
  )

  /**
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    */
  implicit lazy val intBsonOrder: BsonOrder[Int] = fromOrder

  /**
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    */
  implicit lazy val longBsonOrder: BsonOrder[Long] = fromOrder
}
