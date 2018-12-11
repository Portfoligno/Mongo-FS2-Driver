package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.{Order, ~>}

/**
  * An extractor for the bounds based on the `O` context
  */
object Bounded {
  def unapply[O[_], A](interval: Interval[O, A])(implicit O: O[A], ev: O ~> Order): Option[(Bound[A], Bound[A])] =
    interval match {
      case Valued(Some(left @ Bound(l, lc)), Some(right @ Bound(r, rc)))
        if ((i: Int) => if (lc || rc) i <= 0 else i < 0)(ev(O).compare(l, r)) =>
        Some((left, right))

      case _ =>
        None
    }
}
