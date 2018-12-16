package io.github.portfoligno.fs2.mongo.bson

import io.github.portfoligno.fs2.mongo.Wrapped
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import org.bson.types.{ObjectId => UnderlyingObjectId}

case class ObjectId(override val underlying: UnderlyingObjectId) extends Wrapped[UnderlyingObjectId]

object ObjectId {
  import io.github.portfoligno.fs2.mongo.instance.bsonCore._

  lazy val bsonOrderInstance: BsonOrder[ObjectId] =
    BsonOrder.by(_.underlying)
}
