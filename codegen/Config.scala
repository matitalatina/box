package ch.wsl.codegen

object Config{
  // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
  val initScripts = Seq()//Seq("drop-tables.sql","create-tables.sql","populate-tables.sql")
  val url = "jdbc:postgresql:incendi"//;INIT="+initScripts.map("runscript from 'src/sql/"+_+"'").mkString("\\;")
  val jdbcDriver =  "org.postgresql.Driver"
  val slickProfile = slick.driver.PostgresDriver
  val user = "andreaminetti"
  val password = ""
}