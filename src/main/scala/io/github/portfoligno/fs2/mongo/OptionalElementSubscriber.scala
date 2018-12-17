package io.github.portfoligno.fs2.mongo

import org.reactivestreams.{Subscriber, Subscription}

private[mongo]
class OptionalElementSubscriber[A](callback: Either[Throwable, Option[A]] => Unit) extends Subscriber[A] {
  private
  var received: Option[A] = None

  override
  def onSubscribe(s: Subscription): Unit =
    s.request(1)

  override
  def onNext(t: A): Unit =
    received = received.fold(Some(t))(last =>
      throw new IllegalStateException(s"At most one element is expected: $last (last) -> $t (current)")
    )

  override
  def onError(t: Throwable): Unit =
    callback(Left(t))

  override
  def onComplete(): Unit =
    callback(Right(received))
}
