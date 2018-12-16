package io.github.portfoligno.fs2.mongo

import org.reactivestreams.{Subscriber, Subscription}

private[mongo]
class SingleElementSubscriber[A](callback: Either[Throwable, A] => Unit) extends Subscriber[A] {
  private
  var received: Option[A] = None

  override
  def onSubscribe(s: Subscription): Unit =
    s.request(1)

  override
  def onNext(t: A): Unit =
    received = received.fold(Some(t))(
      throw new IllegalStateException("At most one element is expected")
    )

  override
  def onError(t: Throwable): Unit =
    callback(Left(t))

  override
  def onComplete(): Unit =
    callback(received.toRight(
      new NoSuchElementException("At least one element is expected")
    ))
}
