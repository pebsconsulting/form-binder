package com.github.tminglei.bind

import java.util.regex.Pattern
import scala.collection.mutable.HashMap
import org.json4s._

/**
 * Framework internal used util methods (!!!NOTE: be careful if using it externally)
 */
object FrameworkUtils {
  val NAME_ERR_PREFIX = "name: "
  val ILLEGAL_ARRAY_INDEX = """.*\[(\d*[^\d\[\]]+\d*)+\].*""".r
  /** copied from Play! form/mapping */
  val EMAIL_REGEX = """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""".r

  //////////////////////////////////////////////////////////////////////////////////
  val PassValidating: Constraint = (name, data, messages, parentOptions) => Nil

  def isEmptyInput(name: String, data: Map[String, String], inputMode: InputMode): Boolean =
    inputMode match {
      case PolyInput => data.get(name).filterNot(v => v == null || v.isEmpty).isEmpty &&
        data.keys.find(k => k.startsWith(name) && k.length > name.length).isEmpty
      case _: BulkInput => data.keys.find(k => k.startsWith(name) && k.length > name.length).isEmpty
      case _: SoloInput => data.get(name).filterNot(v => v == null || v.isEmpty).isEmpty
      case _  => throw new IllegalArgumentException(s"Illegal InputMode: $inputMode")
    }

  // make an internal converter from `(vString) => value`
  def mkSimpleConverter[T](convert: String => T) =
    (name: String, data: Map[String, String]) => {
      convert(data.get(name).orNull)
    }
  
  // make a constraint from `(label, vString, messages) => [error]` (ps: vString may be NULL/EMPTY)
  def mkSimpleConstraint(validate: (String, String, Messages) => Option[String]) =
    new Constraint with SoloInput {
      def apply(name: String, data: Map[String, String], messages: Messages, options: Options) =
        validate(getLabel(name, messages, options), data.get(name).orNull, messages)
          .map { error => Seq(name -> error) }.getOrElse(Nil)
    }

  // make a pre-processor from `(inputString) => outputString` (ps: inputString may be NULL/EMPTY)
  def mkSimplePreProcessor(process: (String) => String): PreProcessor with SoloInput =
    new PreProcessor with SoloInput {
      def apply(name: String, data: Map[String, String], options: Options) =
        (data - name) + (name -> process(data.get(name).orNull))
    }

  @scala.annotation.tailrec
  def processDataRec(prefix: String, data: Map[String,String], options: Options,
            processors: List[PreProcessor]): Map[String,String] =
    processors match {
      case (process :: rest) => processDataRec(prefix, process(prefix, data, options), options, rest)
      case _  => data
    }

  def validateRec(name: String, data: Map[String, String], messages: Messages, options: Options,
            validates: List[Constraint]): Seq[(String, String)] = validates match {
        case (validate :: rest) => {
          val errors = validate(name, data, messages, options)
          if (errors.isEmpty) validateRec(name, data, messages, options, rest)
          else errors ++ (if (options.eagerCheck.getOrElse(false))
            validateRec(name, data, messages, options, rest) else Nil)
        }
        case _ => Nil
      }

  def extraValidateRec[T](name: String, value: => T, messages: Messages, options: Options,
            validates: List[ExtraConstraint[T]]): Seq[(String, String)] =
    validates match {
      case (validate :: rest) => validate(getLabel(name, messages, options), value, messages) match {
        case Nil    => extraValidateRec(name, value, messages, options, rest)
        case errors => errors.map { case (fieldName, message) => {
          val fullName = if (name.isEmpty) fieldName else if (fieldName.isEmpty) name else name + "." + fieldName
          (fullName, message)
        }} ++ (if (options.eagerCheck.getOrElse(false))
          extraValidateRec(name, value, messages, options, rest) else Nil)
      }
      case _ => Nil
    }

  //////////////////////////// normal processing related //////////////////////////////

  // i18n on: use i18n label, if exists; else use label; else use last field name from full name
  // i18n off: use label; else use last field name from full name
  def getLabel(fullName: String, messages: Messages, options: Options): String = {
    val (parent, name, isArray) = splitName(fullName)
    val default = if (isArray) (splitName(parent)._2 + "[" + name + "]") else name
    if (options.i18n.getOrElse(false)) {
      options._label.flatMap(messages(_).orElse(options._label)).getOrElse(default)
    } else options._label.getOrElse(default)
  }

  // make a Constraint which will try to parse and collect errors
  def parsing[T](parse: String => T, messageKey: String, pattern: String = ""): Constraint with SoloInput =
    mkSimpleConstraint((label, value, messages) => value match {
      case null|"" => None
      case x => {
        try { parse(x); None }
        catch {
          case e: Exception => Some(messages(messageKey).get.format(label, pattern))
        }
      }
    })

  // Computes the available indexes for the given key in this set of data.
  def indexes(key: String, data: Map[String, String]): Seq[Int] = {
    val KeyPattern = ("^" + Pattern.quote(key) + """\[(\d+)\].*$""").r
    data.toSeq.collect { case (KeyPattern(index), _) => index.toInt }.sorted.distinct
  }

  // Computes the available keys for the given prefix in this set of data.
  def keys(prefix: String, data: Map[String, String]): Seq[String] = {
    val KeyPattern = ("^" + Pattern.quote(prefix) + """\.("?[^."]+"?).*$""").r
    data.toSeq.collect { case (KeyPattern(key), _) => key }.distinct
  }
  
  /////////////////////////// json processing related /////////////////////////////////
  
  def json4sToMapData(prefix: String, json: JValue): Map[String, String] = json match {
    case JObject(fields) => {
      fields.map { case (key, value) =>
        json4sToMapData(Option(prefix).filterNot(_.isEmpty).map(_ + ".").getOrElse("") + key, value)
      }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
    case JArray(values) => {
      values.zipWithIndex.map { case (value, i) =>
        json4sToMapData(prefix + "[" + i + "]", value)
      }.foldLeft(Map.empty[String, String])(_ ++ _)
    }
    case JNull => Map.empty
    case JNothing => Map.empty
    case JBool(value) => Map(prefix -> value.toString)
    case JDouble(value) => Map(prefix -> value.toString)
    case JDecimal(value) => Map(prefix -> value.toString)
    case JInt(value) => Map(prefix -> value.toString)
    case JString(value) => Map(prefix -> value.toString)
  }
  
  // convert map tree to json4s JValue
  def mapTreeToJson4s(tree: HashMap[String, Any], useBigDecimalForDouble: Boolean = false): JValue =
    JObject(tree.map {
      case (name, str: String) => JField(name, valueToJson4s(str, useBigDecimalForDouble))
      case (name, obj: HashMap[String, Any]) => {
        val name1 = name.replaceFirst(Pattern.quote(ARRAY_POSTFIX) +"$", "")
        val mJValue =
          if (name.endsWith(ARRAY_POSTFIX)) {
            JArray(obj.toList.sortBy(_._1.toInt).map {
              case (_, value) => valueToJson4s(value, useBigDecimalForDouble)
            })
          } else mapTreeToJson4s(obj, useBigDecimalForDouble)

        JField(name1, mJValue)
      }
      case (name, any) => sys.error(s"unsupported tuple ($name, $any)")
    }.toSeq: _*)

  // convert simple string to json4s JValue
  private val INT_VALUE    = "^[-+]?\\d+$".r
  private val DOUBLE_VALUE = "^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$".r
  private val BOOL_VALUE   = "^true$|^false$".r
  def valueToJson4s(value: Any, useBigDecimalForDouble: Boolean = false): JValue =
    value match {
      case str: String => str match {
        case INT_VALUE()  => JInt(str.toInt)
        case DOUBLE_VALUE(_) => if (useBigDecimalForDouble)
          JDecimal(BigDecimal(str)) else JDouble(str.toDouble)
        case BOOL_VALUE() => JBool(str.toBoolean)
        case _ => JString(str)
      }
      case map: HashMap[String, Any] => mapTreeToJson4s(map, useBigDecimalForDouble)
      case null  => JNull
      case _     => sys.error(s"unsupported value/type: $value")
    }

  // Find a workObject from map tree workList; create one if not exist
  private val ARRAY_POSTFIX = "_$array"
  def workObject(workList: HashMap[String, Any], name: String, isArray: Boolean): HashMap[String, Any] = {
    val name1 = if (isArray) name + ARRAY_POSTFIX else name
    workList.get(name1) match {
      case Some(mapObj) => mapObj.asInstanceOf[HashMap[String, Any]]
      case None => {
        val (parent, self, isArray1) = splitName(name1)
        val parentObj = workObject(workList, parent, isArray1)
        val theObj = HashMap[String, Any]()
        parentObj += (self -> theObj)
        workList  += (name1 -> theObj)
        theObj
      }
    }
  }

  // split a dot separated path name to parent part and self part, with indicating whether it's an array path
  private val OBJECT_ELEM_NAME = "^(.*)\\.([^\\.]+)$".r
  private val ARRAY_ELEM_NAME  = "^(.*)\\[([\\d]+)\\]$".r
  def splitName(name: String): (String, String, Boolean) = name match {
    case ARRAY_ELEM_NAME (name, index)  => (name, index, true)
    case OBJECT_ELEM_NAME(parent, name) => (parent, name, false)
    case _  => ("", name, false)
  }
}
