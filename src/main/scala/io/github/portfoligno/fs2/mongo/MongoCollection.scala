package io.github.portfoligno.fs2.mongo

import cats.Order
import cats.effect.{Resource, Sync}
import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import fs2.Stream
import io.circe.{Decoder, Encoder, ObjectEncoder}
import io.github.portfoligno.fs2.mongo.settings.MongoUri
import org.bson.Document
import spire.math.Interval

import scala.collection.immutable.Seq

class MongoCollection[F[_]](override val underlying: ReactiveCollection[Document])
  extends AnyVal with Wrapper[ReactiveCollection[Document]] {

  def first[A : Decoder](path: Seq[String]): F[Option[A]] = ???

  def last[A : Decoder](path: Seq[String]): F[Option[A]] = ???

  def interval[A : Order : Decoder](path: Seq[String]): F[Interval[A]] = ???


  def findOrdered[A : Order : Encoder, B : Decoder](
    path: List[String], interval: Interval[A]
  )(
    fields: Seq[String]
  ): Stream[F, B] = ???

  def insert[A : ObjectEncoder](documents: Stream[F, A]): F[Unit] = ???
}

object MongoCollection {
  def apply[F[_] : Sync](uri: String): Resource[F, MongoCollection[F]] =
    Resource.liftF(MongoUri(uri)) >>= (MongoCollection(_))

  def apply[F[_] : Sync](uri: MongoUri): Resource[F, MongoCollection[F]] =
    uri.collection.fold(
      raiseResourceError[F, MongoCollection[F]](new IllegalArgumentException("Collection is not defined"))
    )(
      collection => MongoDatabase(uri).map(_(collection))
    )
}