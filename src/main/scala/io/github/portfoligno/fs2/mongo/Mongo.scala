package io.github.portfoligno.fs2.mongo

import cats.effect.{Resource, Sync}
import com.mongodb.reactivestreams.client.MongoClient
import io.github.portfoligno.fs2.mongo.settings.{MongoCredential, MongoSettings, MongoUri}

import scala.collection.immutable.Seq

class Mongo[F[_]] private (override val underlying: MongoClient) extends Wrapped[MongoClient] {
  def apply(name: String): MongoDatabase[F] =
    new MongoDatabase(underlying.getDatabase(name))
}

object Mongo {
  def apply[F[_] : Sync](uri: String): Resource[F, Mongo[F]] =
    Resource.liftF(MongoUri(uri)) >>= (Mongo(_))

  def apply[F[_] : Sync](uri: MongoUri): Resource[F, Mongo[F]] =
    if (uri.database.fold(false)(_ != "admin")) {
      raiseResourceError(new IllegalArgumentException(
        "Database other than 'admin' is specified. Please consider using `MongoDatabase.apply` instead"))
    } else {
      toRawClient(uri).map(new Mongo(_))
    }

  def apply[F[_] : Sync](settings: MongoSettings, credentials: Seq[MongoCredential]): Resource[F, Mongo[F]] =
    if (credentials.nonEmpty && credentials.forall(_.underlying.getSource != "admin")) {
      raiseResourceError(new IllegalArgumentException(
        "Credential of the 'admin' database is required to provide full access."
          + " Please consider using `MongoDatabase.fromCredentials` instead"))
    } else {
      toRawClient(settings, credentials).map(new Mongo(_))
    }
}
