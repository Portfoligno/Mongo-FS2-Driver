package io.github.portfoligno.fs2.mongo

import cats.effect.{Resource, Sync}
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients}

class Mongo(override val underlying: MongoClient) extends AnyVal with Wrapper[MongoClient]

object Mongo {
  def apply[F[_] : Sync](uri: String): Resource[F, Mongo] =
    Resource.liftF(MongoUri(uri)) >>= (Mongo(_))

  def apply[F[_]](uri: MongoUri)(implicit F: Sync[F]): Resource[F, Mongo] =
    Resource.make(
      F.delay(new Mongo(MongoClients.create(uri.underlying)))
    )(
      client => F.delay(client.underlying.close())
    )
}
