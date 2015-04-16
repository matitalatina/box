import sbt._
import Keys._
import Tests._

/**
 * This is a simple sbt setup generating Slick code from the given
 * database before compiling the projects code.
 */
object myBuild extends Build {
  lazy val mainProject = Project(
    id="main",
    base=file("."),
    settings = Project.defaultSettings ++ Seq(
      scalaVersion := "2.11.4",
      organization := "com.ssss",
      name := "rest-backend",
      //scalaOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature"),
      libraryDependencies ++= List(
        "com.typesafe.slick"       %% "slick"            % "2.1.0",
        "com.typesafe.slick"       %% "slick-codegen"    % "2.1.0-RC3",
        "org.slf4j"                %  "slf4j-nop"        % "1.6.4",
        "io.spray"                 %% "spray-can"        % "1.3.3",
        "io.spray"                 %% "spray-routing"    % "1.3.3",
        "io.spray"                 %% "spray-testkit"    % "1.3.3",
        "com.typesafe.akka"        %% "akka-actor"       % "2.3.9",
        "org.specs2"               %% "specs2"           % "2.3.11"    % "test",
        "com.github.tminglei"      %% "slick-pg"         % "0.8.2",
        "postgresql"               %  "postgresql"       % "9.1-901.jdbc4",
        "junit"                    %  "junit"            % "4.8.1"     % "test",
        "ch.qos.logback"           %  "logback-classic"  % "1.1.1",
        "org.scalatest"            %  "scalatest_2.11"   % "2.1.5",
        "org.seleniumhq.selenium"  %  "selenium-java"    % "2.28.0" % "test",
        "org.json4s"               %% "json4s-native"    % "3.2.11"
      ),
      slick <<= slickCodeGenTask, // register manual sbt command
      sourceGenerators in Compile <+= slickCodeGenTask, // register automatic code generation on every compile, remove for only manual use
      unmanagedResourceDirectories in Compile <++= baseDirectory { base =>
        Seq( base / "src/main/webapp" )
      },
      resolvers ++= Seq(
        "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/",
        "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
        "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
        "spray repo" at "http://repo.spray.io/"
      )
    )
  ) 

  // code generation task
  lazy val slick = TaskKey[Seq[File]]("gen-tables")
  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
    val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
    val url = "jdbc:postgresql:incendi" //;INIT=runscript from 'src/main/sql/create.sql'" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
    val user = "postgres"
    val password = "postgres"
    val jdbcDriver = "org.postgresql.Driver"
    val slickDriver = "scala.slick.driver.PostgresDriver"
    val pkg = "ch.wsl.model"
    toError(r.run("scala.slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg, user, password), s.log))
    val fname = outputDir + "/ch/wsl/model/Tables.scala"
    Seq(file(fname))
  }
}