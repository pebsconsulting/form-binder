package com.github.tminglei.bind

import org.slf4j.LoggerFactory
import scala.util.matching.Regex

trait Constraints {
  import FrameworkUtils._

  private val logger = LoggerFactory.getLogger(Constraints.getClass)

  /////////////////////////////////////////  pre-defined constraints  ///////////////////////////////

  def required(message: String = ""): Constraint with Metable[ExtensionMeta] =
    mkConstraintWithMeta((name, data, messages, options) => {
      logger.debug(s"checking required for $name")

      if (isEmptyInput(name, data, options._inputMode)) {
        val errMsg =
          if (!isEmptyInput(name, data, PolyInput)) {
            val msgTemplate = messages("error.wronginput").get
            val simple = getLabel("simple", messages, options)
            val compound = getLabel("compound", messages, options)

            if (options._inputMode == SoloInput)
              msgTemplate.format(simple, compound)
            else
              msgTemplate.format(compound, simple)
          } else {
            (if (message.isEmpty) messages("error.required") else Some(message))
              .get.format(getLabel(name, messages, options))
          }

        Seq((name, errMsg))
      } else Nil
    }, meta = mkExtensionMeta("required"))

  def maxLength(length: Int, message: String = ""): Constraint with Metable[ExtensionMeta] =
    mkSimpleConstraint((label, vString, messages) => {
      logger.debug(s"checking max-length ($length) for '$vString'")

      if (vString != null && vString.length > length) {
        Some( (if (message.isEmpty) messages("error.maxlength") else Some(message))
          .get.format(vString, length))
      } else None
    }, meta = mkExtensionMeta("maxLength", length))

  def minLength(length: Int, message: String = ""): Constraint with Metable[ExtensionMeta] =
    mkSimpleConstraint((label, vString, messages) => {
      logger.debug(s"checking min-length ($length) for '$vString'")

      if (vString != null && vString.length < length) {
        Some( (if (message.isEmpty) messages("error.minlength") else Some(message))
          .get.format(vString, length))
      } else None
    }, meta = mkExtensionMeta("minLength", length))

  def length(length: Int, message: String = ""): Constraint with Metable[ExtensionMeta] =
    mkSimpleConstraint((label, vString, messages) => {
      logger.debug(s"checking length ($length) for '$vString'")

      if (vString != null && vString.length != length) {
        Some( (if (message.isEmpty) messages("error.length") else Some(message))
          .get.format(vString, length))
      } else None
    }, meta = mkExtensionMeta("length", length))

  def oneOf(values: Seq[String], message: String = ""): Constraint with Metable[ExtensionMeta] =
    mkSimpleConstraint((label, vString, messages) => {
      logger.debug(s"checking one of $values for '$vString'")

      if (!values.contains(vString)) {
        Some( (if (message.isEmpty) messages("error.oneof") else Some(message))
          .get.format(vString, values.map("'" + _ + "'").mkString(", ")) )
      } else None
    }, meta = mkExtensionMeta("oneOf", values))

  def email(message: String = ""): Constraint with Metable[ExtensionMeta] = pattern(EMAIL_REGEX, message)

  def pattern(regex: Regex, message: String = ""): Constraint with Metable[ExtensionMeta] =
    mkSimpleConstraint((label, vString, messages) => {
      logger.debug(s"checking pattern '$regex' for '$vString'")

      if (vString != null && regex.findFirstIn(vString).isEmpty) {
        Some( (if (message.isEmpty) messages("error.pattern") else Some(message))
          .get.format(vString, regex.toString))
      } else None
    }, meta = mkExtensionMeta("pattern", regex))

  def patternNot(regex: Regex, message: String = ""): Constraint with Metable[ExtensionMeta] =
    mkSimpleConstraint((label, vString, messages) => {
      logger.debug(s"checking pattern-not '$regex' for '$vString'")

      if (vString != null && regex.findFirstIn(vString).isDefined) {
        Some( (if (message.isEmpty) messages("error.patternnot") else Some(message))
          .get.format(vString, regex.toString))
      } else None
    }, meta = mkExtensionMeta("patternNot", regex))

  def indexInKeys(message: String = ""): Constraint with Metable[ExtensionMeta] =
    mkConstraintWithMeta((name, data, messages, options) => {
      logger.debug(s"checking index in keys for '$name'")

      data.filter(_._1.startsWith(name)).map { case (key, value) =>
        ILLEGAL_ARRAY_INDEX.findFirstIn(key).map { m =>
          (key -> (if (message.isEmpty) messages("error.arrayindex") else Some(message)).get.format(key))
        }
      }.filterNot(_.isEmpty).map(_.get).toSeq
    }, meta = mkExtensionMeta("indexInKeys"))

  ///////////////////////////////////////  pre-defined extra constraints  ////////////////////////////

  def min[T: Ordering](minVal: T, message: String = ""): ExtraConstraint[T] with Metable[ExtensionMeta] =
    mkExtraConstraintWithMeta((label, value, messages) => {
      logger.debug(s"checking min value ($minVal) for $value")

      val ord = Ordering[T]; import ord._
      if (value < minVal) {
        Seq((if (message.isEmpty) messages("error.min") else Some(message))
          .get.format(value, minVal))
      } else Nil
    }, meta = mkExtensionMeta("min", minVal))
  
  def max[T: Ordering](maxVal: T, message: String = ""): ExtraConstraint[T] with Metable[ExtensionMeta] =
    mkExtraConstraintWithMeta((label, value, messages) => {
      logger.debug(s"checking max value ($maxVal) for $value")

      val ord = Ordering[T]; import ord._
      if (value > maxVal) {
        Seq((if (message.isEmpty) messages("error.max") else Some(message))
          .get.format(value, maxVal))
      } else Nil
    }, meta = mkExtensionMeta("max", maxVal))
}

object Constraints extends Constraints