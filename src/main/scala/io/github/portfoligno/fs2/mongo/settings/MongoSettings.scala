package io.github.portfoligno.fs2.mongo.settings

import com.mongodb.MongoClientSettings
import io.github.portfoligno.fs2.mongo.Wrapped

sealed trait MongoSettings extends Any with Wrapped[MongoClientSettings]

object MongoSettings {
  import io.github.portfoligno.fs2.mongo.{settings => outer}

  private[settings]
  case class MongoSettings(override val underlying: MongoClientSettings) extends AnyVal with outer.MongoSettings
}
