package io.github.portfoligno.fs2.mongo.algebra

import cats.arrow.FunctionK
import cats.{Order, ~>}

trait BsonOrder[A] extends Any {
  def toOrder: Order[A]
}

object BsonOrder extends ByteArrayBasedBsonOrderInstances with NumericBsonOrderInstances {
  implicit lazy val bsonOrderFunctionKInstance: BsonOrder ~> Order = new FunctionK[BsonOrder, Order] {
    override
    def apply[A](fa: BsonOrder[A]): Order[A] = fa.toOrder
  }
}
