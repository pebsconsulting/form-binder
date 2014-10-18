package com.github.tminglei.bind

import org.scalatest._

class ConstraintsSpec extends FunSpec with ShouldMatchers {

  describe("test pre-defined constraints") {
    val dummyMessages: Messages = (key) => Some("dummy")

    describe("required") {
      it("simple use") {
        val required = Constraints.required()
        required("", null, dummyMessages) should be (Some("dummy"))
        required("", "", dummyMessages) should be (Some("dummy"))
        required("", "test", dummyMessages) should be (None)
      }

      it("with custom message") {
        val required1 = Constraints.required("%s is required")
        required1("haha", null, dummyMessages) should be (Some("haha is required"))
      }
    }

    describe("maxlength") {
      it("simple use") {
        val maxlength = Constraints.maxlength(10)
        maxlength("", "wetyyuu", dummyMessages) should be (None)
        maxlength("", "wetyettyiiie", dummyMessages) should be (Some("dummy"))
        maxlength("", "tuewerri97", dummyMessages) should be (None)
      }

      it("with custom message") {
        val maxlength1 = Constraints.maxlength(10, "'%s': length cannot > %d")
        maxlength1("haha", "eewryuooerjhy", dummyMessages) should be (Some("'eewryuooerjhy': length cannot > 10"))
      }
    }

    describe("minlength") {
      it("simple use") {
        val minlength = Constraints.minlength(3)
        minlength("", "er", dummyMessages) should be (Some("dummy"))
        minlength("", "ert6", dummyMessages) should be (None)
        minlength("", "tee", dummyMessages) should be (None)
      }

      it("with custom message") {
        val minlength1 = Constraints.minlength(3, "'%s': length cannot < %d")
        minlength1("haha", "te", dummyMessages) should be (Some("'te': length cannot < 3"))
      }
    }

    describe("length") {
      it("simple use") {
        val length = Constraints.length(9)
        length("", "123456789", dummyMessages) should be (None)
        length("", "123", dummyMessages) should be (Some("dummy"))
        length("", "1234567890", dummyMessages) should be (Some("dummy"))
      }

      it("with custom message") {
        val length1 = Constraints.length(9, "'%s': length not equal to %d")
        length1("haha", "123", dummyMessages) should be (Some("'123': length not equal to 9"))
      }
    }

    describe("oneOf") {
      it("simple use") {
        val oneof = Constraints.oneOf(Seq("a","b","c"))
        oneof("", "a", dummyMessages) should be (None)
        oneof("", "t", dummyMessages) should be (Some("dummy"))
        oneof("", null, dummyMessages) should be (Some("dummy"))
      }

      it("with custom message") {
        val oneof1 = Constraints.oneOf(Seq("a","b","c"), "'%s': is not one of %s")
        oneof1("haha", "ts", dummyMessages) should be (Some("'ts': is not one of 'a', 'b', 'c'"))
      }
    }

    describe("pattern") {
      it("simple use") {
        val pattern = Constraints.pattern("^(\\d+)$".r)
        pattern("", "1234657", dummyMessages) should be (None)
        pattern("", "32566y", dummyMessages) should be (Some("dummy"))
        pattern("", "123,567", dummyMessages) should be (Some("dummy"))
      }

      it("with custom message") {
        val pattern1 = Constraints.pattern("^(\\d+)$".r, "'%s' not match '%s'")
        pattern1("haha", "t4366", dummyMessages) should be (Some("'t4366' not match '^(\\d+)$'"))
      }
    }

    /**
     * test cases copied from:
     * http://en.wikipedia.org/wiki/Email_address
     */
    describe("email") {
      val email = Constraints.email("'%s' not valid")

      it("valid email addresses") {
        List(
          "niceandsimple@example.com",
          "very.common@example.com",
          "a.little.lengthy.but.fine@dept.example.com",
          "disposable.style.email.with+symbol@example.com",
          "other.email-with-dash@example.com"//,
          //        "user@localserver",
          // internationalization examples
          //        "Pelé@example.com",  //Latin Alphabet (with diacritics)
          //        "δοκιμή@παράδειγμα.δοκιμή", //Greek Alphabet
          //        "我買@屋企.香港",  //Traditional Chinese Characters
          //        "甲斐@黒川.日本",  //Japanese Characters
          //        "чебурашка@ящик-с-апельсинами.рф"  //Cyrillic Characters
        ).map { emailAddr =>
          email("", emailAddr, dummyMessages) should be (None)
        }
      }

      it("invalid email addresses") {
        List(
          "Abc.example.com", //(an @ character must separate the local and domain parts)
          "A@b@c@example.com", //(only one @ is allowed outside quotation marks)
          "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com", //(none of the special characters in this local part is allowed outside quotation marks)
          "just\"not\"right@example.com", //(quoted strings must be dot separated or the only element making up the local-part)
          """this is"not\allowed@example.com""", //(spaces, quotes, and backslashes may only exist when within quoted strings and preceded by a backslash)
          """this\ still\"not\\allowed@example.com""", //(even if escaped (preceded by a backslash), spaces, quotes, and backslashes must still be contained by quotes)
          "john..doe@example.com", //(double dot before @)
          "john.doe@example..com" //(double dot after @)
        ).map { emailAddr =>
          email("", emailAddr, dummyMessages) should be (Some(s"'$emailAddr' not valid"))
        }
      }
    }
  }

  describe("test pre-defined extra constraints") {
    val dummyMessages: Messages = (key) => Some("dummy")

    describe("min") {
      it("for int, with custom message") {
        val min = Constraints.min(5, "%s cannot < %s")
        min("xx", 6, dummyMessages) should be (Nil)
        min("xx", 3, dummyMessages) should be (Seq("" -> "xx cannot < 5"))
      }

      it("for double, with custom message") {
        val min1 = Constraints.min(5.5d, "%s cannot < %s")
        min1("xx", 6d, dummyMessages) should be (Nil)
        min1("xx", 3d, dummyMessages) should be (Seq("" -> "xx cannot < 5.5"))
      }
    }

    describe("max") {
      it("for int, with custom message") {
        val max = Constraints.max(15, "%s cannot > %s")
        max("xx", 6, dummyMessages) should be (Nil)
        max("xx", 23, dummyMessages) should be (Seq("" -> "xx cannot > 15"))
      }

      it("for double, with custom message") {
        val max1 = Constraints.max(35.5d, "%s cannot > %s")
        max1("xx", 26d, dummyMessages) should be (Nil)
        max1("xx", 37d, dummyMessages) should be (Seq("" -> "xx cannot > 35.5"))
      }
    }
  }
}
