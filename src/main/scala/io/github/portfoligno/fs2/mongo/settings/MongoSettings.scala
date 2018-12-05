package io.github.portfoligno.fs2.mongo.settings

import com.mongodb.MongoClientSettings
import io.github.portfoligno.fs2.mongo.Wrapper

sealed trait MongoSettings extends Any with Wrapper[MongoClientSettings]

object MongoSettings {
  import io.github.portfoligno.fs2.mongo.{settings => outer}

  private[settings]
  case class MongoSettings(override val underlying: MongoClientSettings) extends AnyVal with outer.MongoSettings
}
