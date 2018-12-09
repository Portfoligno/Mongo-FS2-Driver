package io.github.portfoligno.fs2.mongo

import cats.data.OptionT
import cats.effect.Async
import com.mongodb.client.model.Sorts
import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import io.github.portfoligno.fs2.mongo.algebra.BsonOrder
import io.github.portfoligno.fs2.mongo.algebra.interval.{Interval, IntervalT}
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId

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
}
