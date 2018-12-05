package io.github.portfoligno.fs2.mongo.settings

import cats.ApplicativeError
import cats.syntax.functor._
import com.mongodb.connection.netty.NettyStreamFactoryFactory
import com.mongodb.{ConnectionString, MongoClientSettings, MongoCredential}

sealed trait MongoUri {
  def credential: Option[MongoCredential]

  def database: Option[String]

  def collection: Option[String]

  def settings: MongoSettings


  private[mongo]
  def toRawSettingsWithCredential: MongoClientSettings =
    credential.fold(
      settings.underlying
    )(
      MongoClientSettings
        .builder(settings.underlying)
        .credential(_)
        .build()
    )
}

object MongoUri {
  import io.github.portfoligno.fs2.mongo.{settings => outer}

  private
  case class MongoUri(
    override
    val credential: Option[MongoCredential],
    override
    val database: Option[String],
    override
    val collection: Option[String],
    override
    val settings: MongoSettings
  )
    extends outer.MongoUri


  private
  class ParsedUri(uri: String) extends ConnectionString(uri) {
    def credential: Option[MongoCredential] = Option(super.getCredential)

    // Workaround that MongoClientSettings.Builder does not allow un-setting the credential later
    override
    def getCredential: MongoCredential = null
  }

  def apply[F[_]](uri: String)(implicit F: ApplicativeError[F, Throwable]): F[outer.MongoUri] =
    F.catchNonFatal(new ParsedUri(uri)).map(p =>
      MongoUri(
        p.credential,
        Option(p.getDatabase),
        Option(p.getCollection),
        MongoSettings.MongoSettings {
          val builder = MongoClientSettings.builder().applyConnectionString(p)

          // Apply the 'streamType' option (Nullable)
          if ("netty".equalsIgnoreCase(p.getStreamType)) {
            builder.streamFactoryFactory(NettyStreamFactoryFactory.builder().build())
          }
          builder.build()
        })
    )
}
