package io.github.portfoligno.fs2

import cats.{Applicative, ApplicativeError}
import cats.effect.Resource

package object mongo {
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
