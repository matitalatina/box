Postgres REST UI
============================

Usage
-----
```
sbt serve
```
Serve task compiles both client (with fastOptJS) and server then starts the server

Modules
------
- codegen: Code generation from postgres database using slick codegen library
- server: Spray REST server exposing tables of the db
- client: Web UI for the REST APIs

Libraries
------

- [Spray](http://spray.io/)
- [Slick](http://slick.typesafe.com/)
- [JSONSchema](http://json-schema.org/)
- [ScalaJS](http://www.scala-js.org/)
- [ReactJS](https://facebook.github.io/react/)
- [ScalaJS-React](https://github.com/japgolly/scalajs-react)
- [react-jsonschema-form](https://github.com/mozilla-services/react-jsonschema-form)

Based on template [jacobus/s4](https://github.com/jacobus/s4) and [chandu0101/scalajs-react-template](https://github.com/chandu0101/scalajs-react-template)

Presentation
------
[http://wavein.ch/talks/postgresrest/#/](http://wavein.ch/talks/postgresrest/#/)

Knows Issues
-----

If on compile time `StackOverflow` errors appears use the following parameters:
```
sbt -J-Xmx4G -J-XX:MaxMetaspaceSize=1G -J-XX:MaxPermSize=1G -J-XX:+CMSClassUnloadingEnabled -J-Xss3m serve
```
