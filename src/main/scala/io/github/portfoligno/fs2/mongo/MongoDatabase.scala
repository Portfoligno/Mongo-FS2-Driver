package io.github.portfoligno.fs2.mongo

import com.mongodb.reactivestreams.client.{MongoDatabase => ReactiveDatabase}

class MongoDatabase(override val underlying: ReactiveDatabase)
  extends AnyVal with Wrapper[ReactiveDatabase] {

  def apply(collectionName: String): MongoCollection =
    new MongoCollection(underlying.getCollection(collectionName))
}
