package io.github.portfoligno.fs2.mongo.instance

import cats.Order
import cats.instances.all._
import cats.syntax.order._
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder._
import org.bson.BsonBinarySubType
import org.bson.types.{Binary, ObjectId}
import spire.math.UByte

import scala.annotation.tailrec

private[instance]
trait ByteArrayBasedBsonOrderInstances {
  private
  implicit lazy val sameLengthByteArrayOrder: Order[Array[Byte]] = Order.from { (left, right) =>
    val n = left.length // The same as right.length

    @tailrec
    def loop(i: Int): Int =
      if (i < n) {
        val result = UByte(left(i)) compare UByte(right(i))

        if (result != 0) {
          result.toInt
        } else {
          loop(1 + i)
        }
      } else {
        0
      }
    loop(0)
  }


  /**
    * @see [[org.bson.BsonBinaryWriter#doWriteBinaryData]]
    * @see https://docs.mongodb.com/manual/reference/bson-type-comparison-order/#bindata
    */
  implicit lazy val binaryBsonOrder: BsonOrder[Binary] = fromOrder(
    Order.by[Binary, (Array[Byte], UByte)](
      b => (b.getData, UByte(b.getType))
    )(
      // MongoDB sorts BinData in the following order:
      Order.whenEqual(
        // 1. First, the length or size of the data.
        Order.by {
          case (data, subType) =>
            if (subType == UByte(BsonBinarySubType.OLD_BINARY.getValue)) {
              // Legacy binary data use 4 extra bytes
              data.length + 4
            } else {
              data.length
            }
        },
        Order.whenEqual(
          // 2. Then, by the BSON one-byte subtype.
          Order.by(_._2),
          // 3. Finally, by the data, performing a byte-by-byte comparison.
          Order.by(_._1)
        )
      )
    )
  )

  /**
    * @see [[org.bson.BsonBinaryWriter#doWriteObjectId]]
    */
  implicit lazy val objectIdBsonOrder: BsonOrder[ObjectId] = fromOrder(Order.by(_.toByteArray))
}
