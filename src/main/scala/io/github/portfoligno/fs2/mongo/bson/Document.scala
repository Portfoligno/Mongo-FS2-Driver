package io.github.portfoligno.fs2.mongo.bson

import io.github.portfoligno.fs2.mongo.Wrapped
import org.bson.{Document => UnderlyingDocument}

case class Document(override val underlying: UnderlyingDocument) extends Wrapped[UnderlyingDocument]
