package io.github.portfoligno.fs2.mongo.instance

import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder._
import org.bson.types.BSONTimestamp

trait BsonCoreInstances extends ByteArrayBasedBsonOrderInstances with Decimal128BsonOrderInstances {
  /**
    * @see https://docs.mongodb.com/manual/reference/bson-types/#timestamps
    */
  implicit lazy val bsonTimestampBsonOrder: BsonOrder[BSONTimestamp] = fromComparable
}
