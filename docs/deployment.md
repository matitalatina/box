---
title: Deploy
permalink: /deploy/
nav_order: 3
---

# Deploy


## Standalone

Generate the application package with `sbt box`, note that in order to build the correct package for the database the
`box` task should be run when a schema equivalent database is set-up in the config.

Standalone package is availabe in
`server/target/universal/server-1.0.0.zip
`

In the zip you will find a `bin/` directory to start the server use: 
``` bin/boot ```
if you want to provide an external configuration file you may use:
``` bin/boot -Dconfig.file=<path of the file>```

## Cloud foundry 

To deploy to cloud foundry (i.e. Swisscom developer cloud) first package the app with
```sbt box```
then upload it with:  
```cf push -p server/target/universal/server-1.0.0.zip <app-name>```

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

## Docker

The official repository is https://hub.docker.com/r/boxframework/box-framework

You need a to have docker installed in your machine (for desktop use https://www.docker.com/products/docker-desktop)

to run Box using docker first create a configuration
```docker config box-config application.template.conf```
then run box as a service
``` docker service create --config src=aws-server,target=/application.conf --publish published=8080,target=8080 --name box boxframework/box-framework:1.0.9 ```


