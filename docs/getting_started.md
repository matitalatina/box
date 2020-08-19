---
title: Getting started
permalink: /getting_started/
nav_order: 1
---

# Getting started

To start a new project with box framework simply do:
```
sbt new minettiandrea/box.g8
```

After inserting the informations required go to the project folder:
```
cd <project-name>
```
And generate your model
```
sbt generateModel
```

Your application is ready!

Run it locally:
```
sbt run
```
or check our [deploy options](/deploy/)