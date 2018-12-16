package io.github.portfoligno.fs2.mongo

package object instance extends SyntaxUtility {
  object std extends StdInstances
  object bsonCore extends BsonCoreInstances
}
