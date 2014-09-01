package com.github.tminglei.bind

import org.scalatest._

class FieldMappingsSpec extends FunSpec with ShouldMatchers with Constraints with Processors {

  describe("test pre-defined field mappings") {
    val dummyMessages: Messages = (key) => "dummy"

    describe("text") {
      val text = trim pipe_: Mappings.text()

      it("valid data") {
        val data = Map("text" -> "tett ")
        text.validate("text", data, dummyMessages) match {
          case Nil => text.convert("text", data) should be ("tett")
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val nullData = Map[String, String]()
        text.validate("text", nullData, dummyMessages) match {
          case Nil => text.convert("text", nullData) should be (null)
          case err => err should be (Nil)
        }
      }
    }

    describe("boolean") {
      val boolean = Mappings.boolean()

      it("invalid data") {
        val invalidData = Map("boolean" -> "teed")
        boolean.validate("boolean", invalidData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("boolean" -> "dummy"))
        }
      }

      it("valid data") {
        val validData = Map("boolean" -> "true")
        boolean.validate("boolean", validData, dummyMessages) match {
          case Nil => boolean.convert("boolean", validData) should be (true)
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val nullData = Map[String, String]()
        boolean.validate("boolean", nullData, dummyMessages) match {
          case Nil => boolean.convert("boolean", nullData) should be (false)
          case err => err should be (Nil)
        }
      }

      it("empty data") {
        val emptyData = Map("boolean" -> "")
        boolean.validate("boolean", emptyData, dummyMessages) match {
          case Nil => boolean.convert("boolean", emptyData) should be (false)
          case err => err should be (Nil)
        }
      }
    }

    describe("number") {
      val number = (cleanComma pipe_: Mappings.number()).verifying(min(1000), max(10000))

      it("invalid data") {
        val invalidData = Map("number" -> "t12345")
        number.validate("number", invalidData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("number" -> "dummy"))
        }
      }

      it("out-of-scope data") {
        val outScopeData = Map("number" -> "345")
        number.validate("number", outScopeData, dummyMessages) match {
          case Nil => ("out of scope - shouldn't occur!") should be ("")
          case err => err should be (Seq("number" -> "dummy"))
        }
      }

      it("long number data") {
        val number1 = Mappings.number()
        val longNumberData = Map("number" -> "146894532240")
        number1.validate("number", longNumberData, dummyMessages) match {
          case Nil => ("long number - shouldn't occur!") should be ("")
          case err => err should be (Seq("number" -> "dummy"))
        }
      }

      it("valid data w/ comma") {
        val validData = Map("number" -> "3,549")
        number.validate("number", validData, dummyMessages) match {
          case Nil => number.convert("number", validData) should be (3549)
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val nullData = Map[String, String]()
        number.convert("number", nullData) should be (0)
        number.validate("number", nullData, dummyMessages) match {
          case Nil => ("(null->) 0 - shouldn't occur!") should be ("")
          case err => err should be (Seq("number" -> "dummy"))
        }
      }

      it("empty data") {
        val emptyData = Map("number" -> "")
        number.convert("number", emptyData) should be (0)
        number.validate("number", emptyData, dummyMessages) match {
          case Nil => ("(empty->) 0 - shouldn't occur!") should be ("")
          case err => err should be (Seq("number" -> "dummy"))
        }
      }
    }

    describe("double") {
      val double = Mappings.double()

      it("invalid datq") {
        val invalidData = Map("double" -> "tesstt")
        double.validate("double", invalidData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("double" -> "dummy"))
        }
      }

      it("valid data") {
        val validData = Map("double" -> "23545.2355")
        double.validate("double", validData, dummyMessages) match {
          case Nil => double.convert("double", validData) should be (23545.2355d)
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val nullData = Map[String, String]()
        double.validate("double", nullData, dummyMessages) match {
          case Nil => double.convert("double", nullData) should be (0d)
          case err => err should be (Nil)
        }
      }

      it("empty data") {
        val emptyData = Map("double" -> "")
        double.validate("double", emptyData, dummyMessages) match {
          case Nil => double.convert("double", emptyData) should be (0d)
          case err => err should be (Nil)
        }
      }
    }

    describe("float") {
      val float = Mappings.float()

      it("invalid datq") {
        val invalidData = Map("float" -> "tesstt")
        float.validate("float", invalidData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("float" -> "dummy"))
        }
      }

      it("valid data") {
        val validData = Map("float" -> "23545.2355")
        float.validate("float", validData, dummyMessages) match {
          case Nil => float.convert("float", validData) should be (23545.2355f)
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val nullData = Map[String, String]()
        float.validate("float", nullData, dummyMessages) match {
          case Nil => float.convert("float", nullData) should be (0f)
          case err => err should be (Nil)
        }
      }

      it("empty data") {
        val emptyData = Map("float" -> "")
        float.validate("float", emptyData, dummyMessages) match {
          case Nil => float.convert("float", emptyData) should be (0f)
          case err => err should be (Nil)
        }
      }
    }

    describe("long") {
      val long = Mappings.long()

      it("invalid datq") {
        val invalidData = Map("long" -> "tesstt")
        long.validate("long", invalidData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("long" -> "dummy"))
        }
      }

      it("valid data") {
        val validData = Map("long" -> "235452355")
        long.validate("long", validData, dummyMessages) match {
          case Nil => long.convert("long", validData) should be (235452355L)
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val nullData = Map[String, String]()
        long.validate("long", nullData, dummyMessages) match {
          case Nil => long.convert("long", nullData) should be (0L)
          case err => err should be (Nil)
        }
      }

      it("empty data") {
        val emptyData = Map("long" -> "")
        long.validate("long", emptyData, dummyMessages) match {
          case Nil => long.convert("long", emptyData) should be (0L)
          case err => err should be (Nil)
        }
      }
    }

    describe("bigDecimal") {
      val bigDecimal = Mappings.bigDecimal()

      it("invalid datq") {
        val invalidData = Map("bigDecimal" -> "tesstt")
        bigDecimal.validate("bigDecimal", invalidData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("bigDecimal" -> "dummy"))
        }
      }

      it("valid data") {
        val validData = Map("bigDecimal" -> "23545.2355")
        bigDecimal.validate("bigDecimal", validData, dummyMessages) match {
          case Nil => bigDecimal.convert("bigDecimal", validData) should be (BigDecimal("23545.2355"))
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val nullData = Map[String, String]()
        bigDecimal.validate("bigDecimal", nullData, dummyMessages) match {
          case Nil => bigDecimal.convert("bigDecimal", nullData) should be (BigDecimal("0.0"))
          case err => err should be (Nil)
        }
      }

      it("empty data") {
        val emptyData = Map("bigDecimal" -> "")
        bigDecimal.validate("bigDecimal", emptyData, dummyMessages) match {
          case Nil => bigDecimal.convert("bigDecimal", emptyData) should be (BigDecimal("0.0"))
          case err => err should be (Nil)
        }
      }
    }

    describe("bigInt") {
      val bigInt = Mappings.bigInt()

      it("invalid datq") {
        val invalidData = Map("bigInt" -> "tesstt")
        bigInt.validate("bigInt", invalidData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("bigInt" -> "dummy"))
        }
      }

      it("valid data") {
        val validData = Map("bigInt" -> "235452355")
        bigInt.validate("bigInt", validData, dummyMessages) match {
          case Nil => bigInt.convert("bigInt", validData) should be (BigInt("235452355"))
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val nullData = Map[String, String]()
        bigInt.validate("bigInt", nullData, dummyMessages) match {
          case Nil => bigInt.convert("bigInt", nullData) should be (BigInt("0"))
          case err => err should be (Nil)
        }
      }

      it("empty data") {
        val emptyData = Map("bigInt" -> "")
        bigInt.validate("bigInt", emptyData, dummyMessages) match {
          case Nil => bigInt.convert("bigInt", emptyData) should be (BigInt("0"))
          case err => err should be (Nil)
        }
      }
    }

    describe("date") {
      val formatter = new java.text.SimpleDateFormat("yyyy-MM-dd")
      val date = Mappings.date("yyyy-MM-dd").verifying(min(formatter.parse("2000-1-1")), max(formatter.parse("2015-1-1")))

      it("invalid data") {
        val invalidData = Map("date" -> "5/3/2003")
        date.validate("date", invalidData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("date" -> "dummy"))
        }
      }

      it("out-of-scope data") {
        val outScopeData = Map("date" -> "1998-7-1")
        date.validate("date", outScopeData, dummyMessages) match {
          case Nil => ("invalid - shouldn't occur!") should be ("")
          case err => err should be (Seq("date" -> "dummy"))
        }
      }

      it("valid data") {
        val validData = Map("date" -> "2007-8-3")
        date.validate("date", validData, dummyMessages) match {
          case Nil => date.convert("date", validData) should be (formatter.parse("2007-8-3"))
          case err => err should be (Nil)
        }
      }

      it("null data") {
        val date1 = Mappings.date("yyyy-MM-dd")
        val nullData = Map[String, String]()
        date1.validate("date", nullData, dummyMessages) match {
          case Nil => date1.convert("date", nullData) should be (null)
          case err => err should be (Nil)
        }
      }

      it("empty data") {
        val date1 = Mappings.date("yyyy-MM-dd")
        val emptyData = Map("date" -> "")
        date1.validate("date", emptyData, dummyMessages) match {
          case Nil => date1.convert("date", emptyData) should be (null)
          case err => err should be (Nil)
        }
      }
    }
  }
}
