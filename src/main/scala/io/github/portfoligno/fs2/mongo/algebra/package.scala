package io.github.portfoligno.fs2.mongo

import cats.Order

package object algebra {
  private[algebra]
  case class FromOrder[A](override val toOrder: Order[A]) extends AnyVal with BsonOrder[A]
}
