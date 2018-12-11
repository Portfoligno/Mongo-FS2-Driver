package io.github.portfoligno.fs2.mongo.algebra.interval

import cats.{Order, ~>}

/**
  * An extractor to perform emptiness check based on the `O` context
  */
object Empty {
  def unapply[O[_], A](interval: Interval[O, A])(implicit O: O[A], ev: O ~> Order): Boolean =
    interval match {
      case Valued(Some(Bound(l, lc)), Some(Bound(r, rc)))
        if ((i: Int) => if (lc || rc) i > 0 else i >= 0)(ev(O).compare(l, r)) =>
        true

      case _ =>
        false
    }
}
