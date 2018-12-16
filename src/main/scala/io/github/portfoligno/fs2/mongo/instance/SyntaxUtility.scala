package io.github.portfoligno.fs2.mongo.instance

import cats.Order
import cats.syntax.order._
import spire.math.NumberTag

import scala.language.implicitConversions

private[instance]
trait SyntaxUtility {
  private[instance]
  implicit def toNumberTagIdOps[A](a: A): NumberTagIdOps[A] =
    new NumberTagIdOps[A](a)


  private[instance]
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

  private[instance]
  def noneLast[A](implicit A: Order[A]): Order[Option[A]] =
    Order.reverse(noneFirst(Order.reverse(A)))
}

private[instance]
class NumberTagIdOps[A](private val a: A) extends AnyVal {
  def isNaN(implicit A: NumberTag[A]): Boolean =
    A.isNaN(a)

  def isInfinite(implicit A: NumberTag[A]): Boolean =
    A.isInfinite(a)

  def nonNaN(implicit A: NumberTag[A]): Option[A] =
    if (isNaN) None else Some(a)

  def finite(implicit A: NumberTag[A]): Option[A] =
    if (isInfinite) None else Some(a)
}
