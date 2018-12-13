package io.github.portfoligno.fs2.mongo

import cats.effect.{Resource, Sync}
import com.mongodb.reactivestreams.client.{MongoDatabase => ReactiveDatabase}
import io.github.portfoligno.fs2.mongo.settings.{MongoCredential, MongoSettings, MongoUri}

import scala.collection.immutable.Seq

class MongoDatabase[F[_]](override val underlying: ReactiveDatabase) extends Wrapped[ReactiveDatabase] {
  def apply(collectionName: String): MongoCollection[F] =
    new MongoCollection(underlying.getCollection(collectionName))

  def name: String = underlying.getName
}

object MongoDatabase {
  def apply[F[_] : Sync](uri: String): Resource[F, MongoDatabase[F]] =
    Resource.liftF(MongoUri(uri)) >>= (MongoDatabase(_))

  def apply[F[_] : Sync](uri: MongoUri): Resource[F, MongoDatabase[F]] =
    uri.database.fold(
      raiseResourceError[F, MongoDatabase[F]](new IllegalArgumentException("Database is not defined"))
    )(
      database => toRawClient(uri).map(client => new MongoDatabase(client.getDatabase(database)))
    )

  def apply[F[_] : Sync](settings: MongoSettings, credential: MongoCredential): Resource[F, MongoDatabase[F]] =
    toRawClient(settings, Seq(credential)).map(client =>
      new MongoDatabase(client.getDatabase(credential.underlying.getSource))
    )


  def fromCredentials[F[_] : Sync](
    settings: MongoSettings, credentials: Seq[MongoCredential]
  ): Resource[F, Seq[MongoDatabase[F]]] =
    if (credentials.isEmpty) {
      Resource.pure(Seq())
    } else {
      toRawClient(settings, credentials).map(client =>
        credentials.map(c => new MongoDatabase[F](client.getDatabase(c.underlying.getSource)))
      )
    }
}
