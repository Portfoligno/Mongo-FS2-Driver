package io.github.portfoligno.fs2.mongo.algebra

import cats.arrow.FunctionK
import cats.{Order, ~>}

import scala.language.existentials

private[algebra]
trait BsonOrderInstances {
  private
  def toOrder[A](bsonOrder: BsonOrder[A]): Order[A] = bsonOrder.toOrder

  private
  lazy val _bsonOrderToOrderFunctionKInstance: BsonOrder ~> Order =
    FunctionK.lift(toOrder)

  implicit def bsonOrderToOrderFunctionKInstance[F[_]](
    implicit ev: (Order[α] <:< F[α]) forSome { type α }
  ): BsonOrder ~> F =
    _bsonOrderToOrderFunctionKInstance.asInstanceOf[BsonOrder ~> F]
}
