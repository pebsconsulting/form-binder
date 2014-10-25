package com.github.tminglei

package object bind {

  // (messageKey) => [message]
  type Messages = (String) => Option[String]

  // (label, vString, messages) => [error]
  type Constraint = (String, String, Messages) => Option[String]

  // (label, vObject, messages) => errors
  type ExtraConstraint[T] = (String, T, Messages) => Seq[(String, String)]

  // (input) => output
  type PreProcessor = (String) => String

  // (prefix, data) => data
  type BulkPreProcessor = (String, Map[String, String]) => Map[String, String]

  // (data) => touched list
  type TouchedExtractor = (Map[String, String]) => Seq[String]

  // (errors) => R
  type PostErrProcessor[R] = (Seq[(String, String)]) => R

}
