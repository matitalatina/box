---
title: Logging
parent: Documentation
nav_order: 4
---

# Logging


## Server

By default the server logs on the standard output at WARN level.

Logging level may be set in the `box.conf` table with key `logger.level`

### Log to DB

Server logs may also be saved in the `box.log` table, to enable the logging on db set `box.conf` `log.db` to `true` 

## Client 

Same log levels of the server applies but the log is printed only in he browser console

( TODO saving in DB UI logs too, issue [#46](https://github.com/Insubric/box/issues/46))