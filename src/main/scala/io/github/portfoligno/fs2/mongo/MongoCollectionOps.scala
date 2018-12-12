package io.github.portfoligno.fs2.mongo

import cats.data.OptionT
import cats.effect.{Async, ConcurrentEffect}
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.{Filters, InsertOneModel, Projections, Sorts}
import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import fs2.Stream
import fs2.interop.reactivestreams._
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.segment.{Bounded, Segment, SegmentT}
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

  def idInterval(implicit F: Async[F]): SegmentT[F, BsonOrder, ObjectId] =
    SegmentT((firstId, lastId)
      .mapN(Segment.closed[BsonOrder](_, _))
      .fold(Segment.empty[BsonOrder, ObjectId])(identity))


  def findById(
    segment: SegmentT[F, BsonOrder, ObjectId]
  )(
    fields: Seq[String], batchSize: Int
  )(
    implicit F: ConcurrentEffect[F], A: BsonOrder[ObjectId]
  ): Stream[F, Document] =
    Stream.eval(segment.value) >>= {
      case Bounded(_, _, 0) =>
        Stream.empty

      case Bounded(left, right, direction) =>
        val criteria = Seq(
          left.map(b => (if (b.isClosed) Filters.gte _ else Filters.gt _)("_id", b.value)),
          right.map(b => (if (b.isClosed) Filters.lte _ else Filters.lt _)("_id", b.value))
        )
        underlying
          .find(Filters.and(criteria.flatten: _*))
          .projection(Projections.include(fields: _*))
          .sort((if (direction < 0) Sorts.descending(_: String) else Sorts.ascending(_: String))("_id"))
          .batchSize(batchSize)
          .toStream
          .chunkN(batchSize)
          .flatMap(Stream.chunk)
    }


  def insert(documents: Stream[F, Document])(implicit F: ConcurrentEffect[F]): Stream[F, BulkWriteResult] =
    documents
      .map(new InsertOneModel(_))
      .chunks
      .flatMap(c => underlying.bulkWrite(c.toVector.asJava).toStream[F])
}
