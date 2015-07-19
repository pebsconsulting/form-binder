form-binder
===========
[![Build Status](https://travis-ci.org/tminglei/form-binder.svg?branch=master)](https://travis-ci.org/tminglei/form-binder)


Form-binder is a micro data binding and validating framework, easy to use and hack.

> _It was initially created for my [`Scalatra`](https://github.com/scalatra/scalatra)-based project, but it's for general purpose. You can easily integrate and use it with other frameworks._



Features
-------------
- very lightweight, only ~900 lines codes (framework + built-in extensions)
- easy use, no verbose codes, and what you see is what you get
- high customizable, you can extend almost every executing point
- easily extensible, every extension interface is an alias of `FunctionN`
- immutable, you can share mapping definition object safely



Usage
-------------
![form-binder description](https://github.com/tminglei/form-binder/raw/master/form-binder-desc.png)

1. define your binder
2. define your mappings
3. prepare your data
4. bind and consume


> _p.s. every points above (1)/(2)/(3)/(4)/ are all extendable and you can easily customize it._  



Install & Integrate
--------------------
To use `form-binder`, pls add the dependency to your [sbt](http://www.scala-sbt.org/ "slick-sbt") project file:
```scala
libraryDependencies += "com.github.tminglei" %% "form-binder" % "0.9.0"
```

Then you can integrate it with your framework to simplify normal usage. 

Here's the way in my `Scalatra` project:

First, I defined a `FormBindSupport` trait,
```scala
trait MyFormBindSupport extends I18nSupport { self: ScalatraBase =>
  import MyFormBindSupport._

  before() {
    request(BindMessagesKey) = Messages(locale, bundlePath = "i18n/bind-messages")
  }

  def binder(implicit request: HttpServletRequest) =
    expandJsonString(Some("json")) >-: FormBinder(bindMessages.get).withErr(errsToJson4s)

  ///
  private def bindMessages(implicit request: HttpServletRequest): Messages = if (request == null) {
    throw new ScalatraException("There needs to be a request in scope to call bindMessages")
  } else {
    request.get(BindMessagesKey).map(_.asInstanceOf[Messages]).orNull
  }
}
```
Then mix it to my xxxServlet, and use it like this,
```scala
import com.github.tminglei.bind.simple._

class FeatureServlet extends ScalatraServlet with MyFormBindSupport {

  get("/:id") {
    val mappings = tmapping(
      "id" -> long()
    )
    binder.bind(mappings, params).fold(
      errors => holt(400, errors),
      { case (id) =>
        Ok(toJson(repos.features.get(id)))
      }
    )
  }
}
```

_p.s. you can check more integration sample codes under  [/integrations](https://github.com/tminglei/form-binder/tree/master/integrations)._



Build & Test
-------------------
To hack it and make your contribution, you can setup it like this:
```bash
 $ git clone https://github.com/tminglei/form-binder.git
 $ cd form-binder
 $ sbt
...
```
To run the tests, pls execute:
```bash
 $ sbt test
```


How it works
--------------------
### Principle
The core of `form-binder` is `Mapping`, **tree structure** mappings. With **depth-first** algorithm, it was used to validate data and construct the result value object.

### Details

![form-binder description](https://github.com/tminglei/form-binder/raw/master/form-binder-desc.png)

#### Major Components:  
[1] **binder**: facade, used to bind and trigger processing, two major methods: `bind`, `validate`  
[2] **messages**: used to provide error messages  
[3] **mapping**: holding constraints, processors, and maybe child mapping, etc. used to validate/convert data, two types of mappings: `field` and `group`  
[4] **data**: inputting data map  

> _Check [here](https://github.com/tminglei/form-binder/blob/master/src/main/scala/com/github/tminglei/bind/Framework.scala) for framework details._

binder **bind** method signature (return an `Either` and let user to continue processing):
```scala
//bind mappings to data, and return an either, holding validation errors (left) or converted value (right)
def bind[T](mapping: Mapping[T], data: Map[String, String], root: String = ""): Either[R, T]
```

binder **validate** method signature (_validate only_ and not consume converted data):
```scala
//return (maybe processed) errors
def validate[T](mapping: Mapping[T], data: Map[String, String], root: String = "")
```

> _Check [here](https://github.com/tminglei/form-binder/blob/master/src/main/scala/com/github/tminglei/bind/Mappings.scala) for built-in **mapping**s._  

#### Extension Types:  
(1) **ErrProcessor**: used to process error seq, like converting it to json  
(2) **PreProcessor**: used to pre-process data, like omitting `$` from `$3,013`  
(3) **Constraint**: used to validate raw string data  
(4) **ExtraConstraint**: used to valdate converted value  

> _* Check [here](https://github.com/tminglei/form-binder/blob/master/src/main/scala/com/github/tminglei/bind/Processors.scala) for built-in `PreProcessor`/`ErrProcessor`._  
> _**Check [here](https://github.com/tminglei/form-binder/blob/master/src/main/scala/com/github/tminglei/bind/Constraints.scala) for built-in `Constraint`/`ExtraConstraint`._

#### Options/Features:  
1) **label**: `feature`, readable name for current group/field  
2) **mapTo**: `feature`, map converted value to another type  
3) **i18n**: `option`, let label value can be used as a message key to fetch a i18n value from `messages`   
4) **eagerCheck**: `option`, check errors as more as possible  
5) **ignoreEmpty**: `option`, not check empty field/values, especially they're not touched by user  
6) **touched**: `function`, check whether a field was touched by user; if yes, they can't be empty if they're required  

> _* By default, form-binder would return right after encountered a validation error._  
> _** ignoreEmpty + touched, will let form-binder re-check touched empty field/values._  
> _*** if i18n is on, the label you input should be a message key instead of a value._



How to
--------------------
//todo



Acknowledgements
-----------------
- [`Play!`](https://github.com/playframework/playframework) framework development team, for the original idea and implementation;
- [Naoki Takezoe](https://github.com/takezoe) and his [`scalatra-forms`](https://github.com/takezoe/scalatra-forms), a `play-data` implementation for scalatra.


License
---------
The BSD License, Minglei Tu &lt;tmlneu@gmail.com&gt;
