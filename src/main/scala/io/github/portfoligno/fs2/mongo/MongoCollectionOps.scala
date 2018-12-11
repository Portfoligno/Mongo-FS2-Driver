package io.github.portfoligno.fs2.mongo

import cats.data.OptionT
import cats.effect.Async
import com.mongodb.client.model.{Filters, Projections, Sorts}
import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import fs2.Stream
import fs2.interop.reactivestreams._
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.interval._
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

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

  def idInterval(implicit F: Async[F]): IntervalT[F, BsonOrder, ObjectId] =
    IntervalT((firstId, lastId)
      .mapN(Interval.closed[BsonOrder](_, _))
      .fold(Interval.empty[BsonOrder, ObjectId])(identity))


  private
  def findById(
    sorting: String => Bson, interval: IntervalT[F, BsonOrder, ObjectId]
  )(
    fields: Seq[String]
  ): Stream[F, Document] =
    Stream.eval(interval.value) >>= {
      case Valued(left, right) =>
        val criteria = Seq(
          left.map(b => (if (b.isClosed) Filters.gte _ else Filters.gt _)("_id", b.value)),
          right.map(b => (if (b.isClosed) Filters.lte _ else Filters.lt _)("_id", b.value))
        )
        underlying
          .find(Filters.and(criteria.flatten: _*))
          .projection(Projections.include(fields: _*))
          .sort(sorting("_id"))
          .toStream

      case _ =>
        Stream.empty
    }

  def ascendingById(interval: IntervalT[F, BsonOrder, ObjectId])(fields: Seq[String]): Stream[F, Document] =
    findById(Sorts.ascending(_), interval)(fields)

  def descendingById(interval: IntervalT[F, BsonOrder, ObjectId])(fields: Seq[String]): Stream[F, Document] =
    findById(Sorts.descending(_), interval)(fields)
}
