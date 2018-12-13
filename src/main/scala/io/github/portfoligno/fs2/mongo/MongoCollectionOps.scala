package io.github.portfoligno.fs2.mongo

import cats.data.OptionT
import cats.effect.{Async, ConcurrentEffect}
import com.mongodb.client.model.{Filters, InsertOneModel, Sorts}
import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import fs2.interop.reactivestreams._
import fs2.{Chunk, Stream}
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.segment.{Interval, Segment}
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

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
      .map(_.get("_id", classOf[ObjectId]))

  def firstId(implicit F: Async[F]): OptionT[F, ObjectId] =
    boundId(Sorts.ascending(_))

  def lastId(implicit F: Async[F]): OptionT[F, ObjectId] =
    boundId(Sorts.descending(_))


  def findById(segment: Segment[BsonOrder, ObjectId])(batchSize: Int)(
    implicit F: ConcurrentEffect[F], A: BsonOrder[ObjectId]
  ): Stream[F, Document] =
    segment match {
      case Interval(_, _, 0) =>
        Stream.empty

      case Interval(left, right, direction) =>
        val criteria = Seq(
          left.map(b => (if (b.isClosed) Filters.gte _ else Filters.gt _)("_id", b.value)),
          right.map(b => (if (b.isClosed) Filters.lte _ else Filters.lt _)("_id", b.value))
        )
        underlying
          .find(Filters.and(criteria.flatten: _*))
          .sort((if (direction < 0) Sorts.descending(_: String) else Sorts.ascending(_: String))("_id"))
          .batchSize(batchSize)
          .toStream
          .chunkN(batchSize)
          .flatMap(Stream.chunk)
    }


  def insert(chunk: Chunk[Document])(implicit F: ConcurrentEffect[F]): Stream[F, Any] =
    underlying
      .bulkWrite(chunk.map(new InsertOneModel(_)).toVector.asJava)
      .toStream[F]
}
