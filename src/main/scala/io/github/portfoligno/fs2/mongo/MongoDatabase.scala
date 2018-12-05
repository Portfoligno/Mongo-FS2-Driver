package io.github.portfoligno.fs2.mongo

import cats.effect.{Resource, Sync}
import com.mongodb.reactivestreams.client.{MongoDatabase => ReactiveDatabase}
import io.github.portfoligno.fs2.mongo.settings.MongoUri

class MongoDatabase[F[_]](override val underlying: ReactiveDatabase)
  extends AnyVal with Wrapper[ReactiveDatabase] {

  def apply(collectionName: String): MongoCollection[F] =
    new MongoCollection(underlying.getCollection(collectionName))
}

object MongoDatabase {
  def apply[F[_] : Sync](uri: String): Resource[F, MongoDatabase[F]] =
    Resource.liftF(MongoUri(uri)) >>= (MongoDatabase(_))

  def apply[F[_]](uri: MongoUri)(implicit F: Sync[F]): Resource[F, MongoDatabase[F]] =
    uri.database.fold(
      raiseResourceError[F, MongoDatabase[F]](new IllegalArgumentException("Database is not defined"))
    )(
      database => Mongo.fromUri(uri).map(_(database))
    )
}
