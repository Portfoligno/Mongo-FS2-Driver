package io.github.portfoligno.fs2.mongo.settings

import com.mongodb.{MongoCredential => UnderlyingCredential}
import io.github.portfoligno.fs2.mongo.Wrapper

case class MongoCredential(override val underlying: UnderlyingCredential) extends Wrapper[UnderlyingCredential]
