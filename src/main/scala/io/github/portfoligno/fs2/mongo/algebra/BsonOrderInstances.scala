package io.github.portfoligno.fs2.mongo.algebra

import cats.arrow.FunctionK
import cats.{Order, ~>}

private[algebra]
trait BsonOrderInstances {
  implicit lazy val bsonOrderFunctionKInstance: BsonOrder ~> Order = new FunctionK[BsonOrder, Order] {
    override
    def apply[A](fa: BsonOrder[A]): Order[A] = fa.toOrder
  }
}
