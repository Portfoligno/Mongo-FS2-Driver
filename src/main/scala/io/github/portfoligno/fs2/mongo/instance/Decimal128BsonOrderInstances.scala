package io.github.portfoligno.fs2.mongo.instance

import cats.Order
import cats.syntax.either._
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder._
import org.bson.types.Decimal128
import spire.math.NumberTag.BuiltinFloatTag

import scala.Function.const

private[instance]
trait Decimal128BsonOrderInstances {
  private
  val NAN = 0x7c00000000000000L
  private
  val INFINITY = 0x7800000000000000L
  private
  val NEGATIVE_INFINITE = 0xf800000000000000L

  private
  implicit def bigDecimalOrder: Order[java.math.BigDecimal] = Order.fromComparable

  private
  implicit object Decimal128NumberTag extends BuiltinFloatTag[Decimal128](
    Decimal128.POSITIVE_ZERO,
    Decimal128.fromIEEE754BIDEncoding(0x5fffed09bead87c0L, 0x378d8e63ffffffffL),
    Decimal128.fromIEEE754BIDEncoding(0xdfffed09bead87c0L, 0x378d8e63ffffffffL),
    Decimal128.NaN,
    Decimal128.POSITIVE_INFINITY,
    Decimal128.NEGATIVE_INFINITY
  ) {
    override
    def isInfinite(a: Decimal128): Boolean = (a.getHigh & NAN) == INFINITY

    override
    def isNaN(a: Decimal128): Boolean = a.isNaN
  }

  private
  def isNegativeInfinity(a: Decimal128): Boolean = (a.getHigh & NEGATIVE_INFINITE) == NEGATIVE_INFINITE


  /**
    * NaN < -Infinity < Finite numbers < +Infinity
    *
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#numeric-types
    * @see https://en.wikipedia.org/wiki/Decimal128_floating-point_format
    */
  implicit lazy val decimal128BsonOrder: BsonOrder[Decimal128] = fromOrder(
    Order.by[Decimal128, Option[Decimal128]](
      _.nonNaN
    )(
      noneFirst(Order.by[Decimal128, Option[Decimal128]](
        a => if (isNegativeInfinity(a)) None else Some(a)
      )(
        noneFirst(Order.by[Decimal128, Option[Decimal128]](
          _.finite
        )(
          noneLast(Order.by(a =>
            // Only finite values will reach here
            Either
              .catchOnly[ArithmeticException](a.bigDecimalValue())
              .valueOr(const(java.math.BigDecimal.ZERO))
          ))
        ))
      ))
    )
  )
}
