package io.github.portfoligno.fs2.mongo.algebra

import cats.Order

trait BsonOrder[A] extends Any {
  def toOrder: Order[A]
}

object BsonOrder extends ByteArrayBsonOrderInstances
