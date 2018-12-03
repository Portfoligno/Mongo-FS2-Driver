package io.github.portfoligno.fs2.mongo

import cats.effect.{Resource, Sync}
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients}

class Mongo[F[_]](override val underlying: MongoClient) extends AnyVal with Wrapper[MongoClient] {
  def apply(name: String): MongoDatabase[F] =
    new MongoDatabase(underlying.getDatabase(name))
}

object Mongo {
  def apply[F[_] : Sync](uri: String): Resource[F, Mongo[F]] =
    Resource.liftF(MongoUri(uri)) >>= (Mongo(_))

  def apply[F[_]](uri: MongoUri)(implicit F: Sync[F]): Resource[F, Mongo[F]] =
    Resource.make(
      F.delay(new Mongo[F](MongoClients.create(uri.underlying)))
    )(
      client => F.delay(client.underlying.close())
    )
}
