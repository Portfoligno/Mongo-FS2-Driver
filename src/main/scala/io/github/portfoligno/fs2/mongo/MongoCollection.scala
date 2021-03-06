package io.github.portfoligno.fs2.mongo

import cats.effect.{Resource, Sync}
import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import io.github.portfoligno.fs2.mongo.settings.MongoUri
import org.bson.Document

class MongoCollection[F[_]](override val underlying: ReactiveCollection[Document]) extends MongoCollectionOps[F]

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
