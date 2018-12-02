package io.github.portfoligno.fs2

import cats.effect.Resource

package object mongo {
  // Ad-hoc helpers to avoid relying on partial-unification for syntax highlighting
  private[mongo]
  implicit class ResourceOps[F[_], A](private val resource: Resource[F, A]) extends AnyVal {
    def >>=[B](f: A => Resource[F, B]): Resource[F, B] =
      resource.flatMap(f)
  }
}
