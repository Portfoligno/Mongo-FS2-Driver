package io.github.portfoligno.fs2.mongo

import cats.ApplicativeError
import com.mongodb.ConnectionString

class MongoUri(override val underlying: ConnectionString) extends AnyVal with Wrapper[ConnectionString]

object MongoUri {
  def apply[F[_]](uri: String)(implicit F: ApplicativeError[F, Throwable]): F[MongoUri] =
    F.catchNonFatal(new MongoUri(new ConnectionString(uri)))
}
