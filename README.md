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

- [Akka-http](https://doc.akka.io/docs/akka-http/current/)
- [Slick](http://slick.lightbend.com/)
- [ScalaJS](http://www.scala-js.org/)
- [UDash](http://udash.io/)


Deploy
------
- Cloud foundry: `cf push -p server/target/universal/server-1.0.0.zip <app-name>`

Set Env variables:
- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `DB_SCHEMA`
- `BOX_DB_URL`
- `BOX_DB_USER`
- `BOX_DB_PASSWORD`
- `BOX_DB_SCHEMA`

Presentation
------
[http://wavein.ch/talks/postgresrest/#/](http://wavein.ch/talks/postgresrest/#/)

Knows Issues
-----

If on compile time `StackOverflow` errors appears use the following parameters:
```
sbt -J-Xmx4G -J-XX:MaxMetaspaceSize=1G -J-XX:MaxPermSize=1G -J-XX:+CMSClassUnloadingEnabled -J-Xss3m serve
```


Before compilation after adding JS dependencies call  
```
sbt clinet/packageJSDependencies
```


