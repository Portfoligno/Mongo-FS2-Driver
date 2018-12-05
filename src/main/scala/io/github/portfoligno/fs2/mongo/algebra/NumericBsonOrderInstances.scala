package io.github.portfoligno.fs2.mongo.algebra

import cats.Order
import org.bson.types.Decimal128
import spire.std.any._

trait NumericBsonOrderInstances extends BsonOrderInstances {
  private
  implicit def bigDecimalOrder: Order[java.math.BigDecimal] = Order.fromComparable


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

  /**
    * NaN < -Infinity < Finite numbers < +Infinity
    *
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    * @see https://github.com/mongodb/specifications/blob/master/source/bson-decimal128/decimal128.rst
    */
  implicit lazy val decimal128BsonOrder: BsonOrder[Decimal128] = fromOrder(
    Order.by(_.bigDecimalValue)
  )
}
