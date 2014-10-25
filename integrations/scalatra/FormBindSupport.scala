package com.example

import javax.servlet.http.HttpServletRequest

import org.scalatra.{ScalatraException, ScalatraBase}
import org.scalatra.i18n.{Messages, I18nSupport}
import com.github.tminglei.bind.simple._

object MyFormBindSupport {
  val BindMessagesKey = "bind-messages"
}

trait MyFormBindSupport extends I18nSupport { this: ScalatraBase =>
  import MyFormBindSupport._

  before() {
    request(BindMessagesKey) = Messages(locale, bundlePath = "i18n/bind-messages")
  }

  def binder(implicit request: HttpServletRequest) =
    expandJsonData("json") >>: FormBinder(bindMessages.get).withErr(errsToJson4s)

  ///
  private def bindMessages(implicit request: HttpServletRequest): Messages = if (request == null) {
    throw new ScalatraException("There needs to be a request in scope to call bindMessages")
  } else {
    request.get(BindMessagesKey).map(_.asInstanceOf[Messages]).orNull
  }
}
