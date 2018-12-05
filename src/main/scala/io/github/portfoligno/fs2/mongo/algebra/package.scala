package io.github.portfoligno.fs2.mongo

import cats.Order
import cats.effect.Resource
import cats.syntax.order._
import spire.math.NumberTag

package object algebra {
  private[algebra]
  implicit class NumberTagIdOps[A](private val a: A) extends AnyVal {
    def isNaN(implicit A: NumberTag[A]): Boolean =
      A.isNaN(a)

    def isInfinite(implicit A: NumberTag[A]): Boolean =
      A.isInfinite(a)

    def nonNaN(implicit A: NumberTag[A]): Option[A] =
      if (isNaN) None else Some(a)

    def finite(implicit A: NumberTag[A]): Option[A] =
      if (isInfinite) None else Some(a)
  }


  private[algebra]
  def noneFirst[A : Order]: Order[Option[A]] =
    Order.from((left, right) =>
      if (left.isDefined) {
        if (right.isDefined) {
          0
        }
        else {
          -1
        }
      } else {
        if (right.isDefined) {
          1
        } else {
          left.get compare right.get
        }
      }
    )

  private[algebra]
  def noneLast[A](implicit A: Order[A]): Order[Option[A]] =
    Order.reverse(noneFirst(Order.reverse(A)))


  private[algebra]
  case class FromOrder[A](override val toOrder: Order[A]) extends AnyVal with BsonOrder[A]
}
