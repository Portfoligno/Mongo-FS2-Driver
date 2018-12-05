package io.github.portfoligno.fs2.mongo.algebra

import cats.Order
import cats.instances.all._
import org.bson.types.Decimal128

trait NumericBsonOrderInstances extends BsonOrderInstances {
  private
  implicit def bigDecimalOrder: Order[java.math.BigDecimal] = Order.fromComparable


  /**
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    */
  implicit lazy val doubleBsonOrder: BsonOrder[Double] = fromOrder

  /**
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    */
  implicit lazy val intBsonOrder: BsonOrder[Int] = fromOrder

  /**
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    */
  implicit lazy val longBsonOrder: BsonOrder[Long] = fromOrder

  /**
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    */
  implicit lazy val decimal128BsonOrder: BsonOrder[Decimal128] =
    fromOrder(Order.by(_.bigDecimalValue))
}
