package io.github.portfoligno.fs2.mongo

import cats.data.OptionT
import cats.effect.{Async, ConcurrentEffect}
import cats.instances.all._
import cats.syntax.compose._
import cats.syntax.functor._
import com.mongodb.bulk.BulkWriteResult
import com.mongodb.client.model.{Filters, InsertOneModel, Projections, Sorts}
import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import fs2.interop.reactivestreams._
import fs2.{Chunk, Stream}
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.interval.{Interval, Projection}
import io.github.portfoligno.fs2.mongo.bson.{Document, ObjectId}
import io.github.portfoligno.fs2.mongo.result.WriteResult
import org.bson.conversions.Bson
import org.bson.types.{ObjectId => UnderlyingObjectId}
import org.bson.{Document => UnderlyingDocument}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq
import scala.math.signum

private[mongo]
trait MongoCollectionOps[F[_]] extends Any with Wrapped[ReactiveCollection[UnderlyingDocument]] {
  private
  def boundId(sort: String => Bson)(implicit F: Async[F]): OptionT[F, ObjectId] =
    OptionT
      .apply[F, UnderlyingDocument](F.async(callback =>
        underlying
          .find()
          .projection(Projections.include("_id"))
          .sort(sort("_id"))
          .first()
          .subscribe(new OptionalElementSubscriber(callback))
      ))
      .map(ObjectId.apply _ <<< (_.get("_id", classOf[UnderlyingObjectId])))

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
          .find(criteria.flatten match {
            // Special handling since `Filters.and()` is translated to `{$and: []}`, which is an error
            case Seq() =>
              null

            case seq =>
              Filters.and(seq: _*)
          })
          .sort(signum(direction) match {
            case 1 =>
              Sorts.ascending("_id")

            case -1 =>
              Sorts.descending("_id")

            case _ =>
              null // No sorts
          })
          .batchSize(batchSize)
          .toStream
          .map(Document)
          .chunkN(batchSize)
          .flatMap(Stream.chunk)
    }


  def insert(chunk: Chunk[Document])(implicit F: Async[F]): OptionT[F, WriteResult] =
    if (chunk.isEmpty) {
      OptionT.none
    } else {
      OptionT.liftF(F
        .async[BulkWriteResult](callback =>
          underlying
            .bulkWrite(chunk
              .map((new InsertOneModel(_: UnderlyingDocument)) <<< (_.underlying))
              .toVector
              .asJava)
            .subscribe(new SingleElementSubscriber(callback))
        )
        .map(WriteResult))
    }
}
