---
title: Development
permalink: /development/
nav_order: 2
---

# Development

BOX is completely written in Scala (Scala and Scala.js).

To extend the user inteface a widgets system is in place

## Local server

```
sbt serve
```
Serve task compiles both client (with fastOptJS) and server then starts the server

## Modules

- codegen: Code generation from postgres database using slick codegen library
- server: Spray REST server exposing tables of the db
- client: Web UI for the REST APIs

## Libraries


- [Akka-http](https://doc.akka.io/docs/akka-http/current/)
- [Slick](http://slick.lightbend.com/)
- [ScalaJS](http://www.scala-js.org/)
- [UDash](http://udash.io/)


### Knows Issues


If on compile time `StackOverflow` errors appears use the following parameters:
```
sbt -J-Xmx4G -J-XX:MaxMetaspaceSize=1G -J-XX:MaxPermSize=1G -J-XX:+CMSClassUnloadingEnabled -J-Xss3m serve
```


Before compilation after adding JS dependencies call  
```
sbt client/packageJSDependencies
```
