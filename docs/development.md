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


## Deploy

Generate the application package with `sbt box`, note that in order to build the correct package for the database the
`box` task should be run when a schema equivalent database is set-up in the config.

Standalone package is availabe in
`server/target/universal/server-1.0.0.zip
`

Cloud providers:
- Cloud foundry (Swisscom developer cloud): `cf push -p server/target/universal/server-1.0.0.zip <app-name>`

Database configuration can be done in `resources/application.conf`, a template of `application.conf` is provided in
`application.template.conf`, we therefore advise to use env variables to setup the database connection

Set Env variables:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `DB_SCHEMA`
- `BOX_DB_URL`
- `BOX_DB_USER`
- `BOX_DB_PASSWORD`
- `BOX_DB_SCHEMA`


### Knows Issues


If on compile time `StackOverflow` errors appears use the following parameters:
```
sbt -J-Xmx4G -J-XX:MaxMetaspaceSize=1G -J-XX:MaxPermSize=1G -J-XX:+CMSClassUnloadingEnabled -J-Xss3m serve
```


Before compilation after adding JS dependencies call  
```
sbt clinet/packageJSDependencies
```
