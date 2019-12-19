BOX 
============================
Bring the power of PostgreSQL to the web

## Concept

## What is BOX 
The goal of BOX is to provide a framework that enables non developers to quickly create and modify complete and complex
web applications

BOX is a software divided in two pieces, UI (user interface) and server, that lays on top of PostgreSQL. The UI can run
in any modern browser (Chrome, Firefox, Edge, Safari, IE11) and in mobile too.
The server run on the JVM (Java Virtual Machine) so it can be run should run in any platform (Linux, Windows, macOS)
but test are done only in Linux 

## Features


* Web based UI
* Exploit the features and the knowledge of PostgreSQL
    * Robust and complete SQL DBMS
    * Open source
    * Row level access, every user can access only to the row of the tables that he is allowed 
* Automatic mask generation for tables and views
* Custom database masks generation with support of child tables
* Data exports in CSV
* Reports in HTML and PDF
* Multi language support
* Role management, customize user interface for each role
* Import data from external systems
* API for integration with others system
* Mobile support


## Development

BOX is completely written in Scala (Scala and Scala.js).

To extend the user inteface a widgets system is in place

### Local server

```
sbt serve
```
Serve task compiles both client (with fastOptJS) and server then starts the server

### Modules

- codegen: Code generation from postgres database using slick codegen library
- server: Spray REST server exposing tables of the db
- client: Web UI for the REST APIs

### Libraries


- [Akka-http](https://doc.akka.io/docs/akka-http/current/)
- [Slick](http://slick.lightbend.com/)
- [ScalaJS](http://www.scala-js.org/)
- [UDash](http://udash.io/)


### Deploy

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

### Reference

##### Dynamic select fields
Lookup tables can be queried dynamically by adding a JSONQuery to the field
and setting some parameters with `#fieldname`, the system will substitute the `#fieldname` parameter
with the current value and update the lookup values.

Example:
```
{
    "filter": [
        {
            "column": "enddate",
            "operator": "<=",
            "value": #date
        },
        {
            "column": "startdate",
            "operator": ">=",
            "value": #date
        }
    ],
    "sort": [],
    "paging": {
        "pageLength": 500,
        "currentPage": 1
    }
}
```

#### Knows Issues


If on compile time `StackOverflow` errors appears use the following parameters:
```
sbt -J-Xmx4G -J-XX:MaxMetaspaceSize=1G -J-XX:MaxPermSize=1G -J-XX:+CMSClassUnloadingEnabled -J-Xss3m serve
```


Before compilation after adding JS dependencies call  
```
sbt clinet/packageJSDependencies
```


## License

Box framework is released with Apache 2.0 License

