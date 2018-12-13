package io.github.portfoligno.fs2.mongo.result

import com.mongodb.bulk.BulkWriteResult
import io.github.portfoligno.fs2.mongo.Wrapped

case class WriteResult(override val underlying: BulkWriteResult) extends Wrapped[BulkWriteResult]
