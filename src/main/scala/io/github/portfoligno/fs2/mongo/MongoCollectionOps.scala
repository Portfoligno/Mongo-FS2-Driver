package io.github.portfoligno.fs2.mongo

import cats.data.OptionT
import cats.effect.{Async, ConcurrentEffect}
import com.mongodb.client.model.{Filters, InsertOneModel, Sorts}
import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import fs2.interop.reactivestreams._
import fs2.{Chunk, Stream}
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.interval.{Interval, Projection}
import io.github.portfoligno.fs2.mongo.bson.ObjectId
import io.github.portfoligno.fs2.mongo.result.WriteResult
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.{ObjectId => UnderlyingObjectId}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq
import scala.math.signum

private[mongo]
trait MongoCollectionOps[F[_]] extends Any with Wrapped[ReactiveCollection[Document]] {
  private
  def boundId(sorting: String => Bson)(implicit F: Async[F]): OptionT[F, ObjectId] =
    OptionT
      .apply[F, Document](F.async(callback =>
        underlying
          .find()
          .sort(sorting("_id"))
          .first()
          .subscribe(new OptionalElementSubscriber(callback))
      ))
      .map(document => ObjectId(document.get("_id").asInstanceOf[UnderlyingObjectId]))

  def firstId(implicit F: Async[F]): OptionT[F, ObjectId] =
    boundId(Sorts.ascending(_))

  def lastId(implicit F: Async[F]): OptionT[F, ObjectId] =
    boundId(Sorts.descending(_))


  def findById(interval: Interval[BsonOrder, ObjectId])(batchSize: Int)(
    implicit F: ConcurrentEffect[F], A: BsonOrder[ObjectId]
  ): Stream[F, Document] =
    interval match {
      case Projection(_, _, 0) =>
        Stream.empty

      case Projection(left, right, direction) =>
        val criteria = Seq(
          left.map(b => (if (b.isClosed) Filters.gte _ else Filters.gt _)("_id", b.value.underlying)),
          right.map(b => (if (b.isClosed) Filters.lte _ else Filters.lt _)("_id", b.value.underlying))
        )
        underlying
          .find(Filters.and(criteria.flatten: _*))
          .sort(signum(direction) match {
            case 1 =>
              Sorts.ascending("_id")

            case -1 =>
              Sorts.descending("_id")

            case _ =>
              null // No sorting
          })
          .batchSize(batchSize)
          .toStream
          .chunkN(batchSize)
          .flatMap(Stream.chunk)
    }


  def insert(chunk: Chunk[Document])(implicit F: ConcurrentEffect[F]): Stream[F, WriteResult] =
    underlying
      .bulkWrite(chunk.map(new InsertOneModel(_)).toVector.asJava)
      .toStream
      .map(WriteResult)
}
