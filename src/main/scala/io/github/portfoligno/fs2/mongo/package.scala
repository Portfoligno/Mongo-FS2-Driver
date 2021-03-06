package io.github.portfoligno.fs2

import cats.effect.{Async, Resource, Sync}
import cats.{Applicative, ApplicativeError}
import com.mongodb.MongoClientSettings
import com.mongodb.async.client.{MongoClientSettings => LegacyClientSettings}
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients}
import io.github.portfoligno.fs2.mongo.settings.{MongoCredential, MongoSettings, MongoUri}

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

package object mongo {
  private
  lazy val legacySettingsBuilderFactory: MongoClientSettings => LegacyClientSettings.Builder = {
    val constructor = classOf[LegacyClientSettings.Builder].getDeclaredConstructor(classOf[MongoClientSettings])
    constructor.setAccessible(true)
    constructor.newInstance(_)
  }

  private
  def toRawSettings(settings: MongoSettings, credential: MongoCredential): MongoClientSettings =
    MongoClientSettings
      .builder(settings.underlying)
      .credential(credential.underlying)
      .build()

  private
  def toRawLegacySettings(settings: MongoSettings, credentials: Seq[MongoCredential]): LegacyClientSettings =
    legacySettingsBuilderFactory(settings.underlying)
      .credentialList(credentials.map(_.underlying).asJava)
      .build()


  private[mongo]
  def toRawClient[F[_] : Sync](uri: MongoUri): Resource[F, MongoClient] =
    toRawClient(uri.settings, uri.credential.toList)

  private[mongo]
  def toRawClient[F[_]](
    settings: MongoSettings, credentials: Seq[MongoCredential]
  )(
    implicit F: Sync[F]
  ): Resource[F, MongoClient] =
    Resource.make(
      credentials match {
        case Seq() =>
          F.delay(MongoClients.create(settings.underlying))
        case Seq(credential) =>
          F.delay(MongoClients.create(toRawSettings(settings, credential)))
        case _ =>
          // The only way to use multiple credentials
          F.delay(MongoClients.create(toRawLegacySettings(settings, credentials)))
      }
    )(
      client => F.delay(client.close())
    )


  // Ad-hoc helpers to avoid relying on partial-unification for syntax highlighting
  private[mongo]
  def raiseResourceError[F[_], A](e: Throwable)(implicit F: ApplicativeError[F, Throwable]): Resource[F, A] =
    Resource.applyCase(F.raiseError(e))

  private[mongo]
  implicit class ResourceOps[F[_], A](private val resource: Resource[F, A]) extends AnyVal {
    def >>=[B](f: A => Resource[F, B]): Resource[F, B] =
      resource.flatMap(f)

    def map[B](f: A => B)(implicit F: Applicative[F]): Resource[F, B] =
      resource.flatMap(a => Resource.pure(f(a)))
  }
}
