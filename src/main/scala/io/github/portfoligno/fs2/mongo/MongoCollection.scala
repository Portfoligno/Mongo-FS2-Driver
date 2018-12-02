package io.github.portfoligno.fs2.mongo

import com.mongodb.reactivestreams.client.{MongoCollection => ReactiveCollection}
import org.bson.Document

class MongoCollection(override val underlying: ReactiveCollection[Document])
  extends AnyVal with Wrapper[ReactiveCollection[Document]]
