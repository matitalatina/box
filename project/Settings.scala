import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._


//scalaJs version is setinto the plugin.sbt file


/**
  * Application settings. Configure the build for your application here.
  * You normally don't have to touch the actual build definition after this.
  */
object Settings {
  /** The name of your application */
  val name = "postgres-rest-ui"

  /** The version of your application */
  val version = "1.0.0"


  val scalacOptions = Seq(
    "-feature",
    "-language:postfixOps"
  )

  /** Options for the scala compiler */
  val scalacOptionsServer = scalacOptions ++ Seq(
    "-Yrangepos",
    "-language:existentials"
  )




  /** Declare global dependency versions here to avoid mismatches in multi part dependencies */
  object versions {

    //General
    val scala = "2.12.4"
    val ficus = "1.4.0"

    //HTTP actors
    val akka = "2.5.9"
    val akkaHttp = "10.1.0-RC2"
    val akkaHttpJson = "1.12.0"

    //Testing
    val specs2 = "3.8.6"
    val junit = "4.12"
    val scalatest = "3.0.1"
    val selenium = "2.53.1"

    //logs
    val logback = "1.1.7"

    //json parsers
    val circe = "0.9.3"

    //database
    val postgres = "9.4.1211"
    val slick = "3.2.1"

    //frontend
    val scalaCss = "0.5.3-RC1"

    //js
    val bootstrap =  "3.3.1-1"

    val udash = "0.5.0"
    val udashJQuery = "1.1.0"

    val scribe = "2.1.0"

  }

  /**
    * These dependencies are shared between JS and JVM projects
    * the special %%% function selects the correct version for each project
    */
  val sharedJVMJSDependencies = Def.setting(Seq(
    "io.udash"                 %%% "udash-core-shared" % versions.udash,
    "io.circe" %%% "circe-core" % versions.circe,
    "io.circe" %%% "circe-generic" % versions.circe,
    "io.circe" %%% "circe-parser" % versions.circe,
    "com.outr" %%% "scribe" % versions.scribe
  ))

  val sharedJVMCodegenDependencies = Def.setting(Seq(
    "com.typesafe.slick"       %% "slick"           % versions.slick,
    "org.postgresql"           %  "postgresql"      % versions.postgres,
    "com.iheart"               %% "ficus"           % versions.ficus
  ))

  val codegenDependecies = Def.setting(sharedJVMCodegenDependencies.value ++ Seq(
    "com.typesafe.slick" %% "slick-codegen" % versions.slick
  ))

  /** Dependencies only used by the JVM project */
  val jvmDependencies = Def.setting(sharedJVMCodegenDependencies.value ++ Seq(
    "org.scala-lang"           % "scala-reflect"     % versions.scala,
    "com.typesafe.akka"        %% "akka-http-core"   % versions.akkaHttp,
    "de.heikoseeberger"        %% "akka-http-circe"  % versions.akkaHttpJson,
    "com.typesafe.akka"        %% "akka-actor"       % versions.akka,
    "com.typesafe.akka"        %% "akka-stream"      % versions.akka,
    "com.softwaremill.akka-http-session" %% "core" % "0.5.3",
    "io.circe"                 %% "circe-core"       % versions.circe,
    "io.circe"                 %% "circe-generic"    % versions.circe,
    "io.circe"                 %% "circe-parser"     % versions.circe,
    "io.udash"                 %% "udash-rpc-backend" % versions.udash,
    "org.webjars"              % "webjars-locator"   % "0.32",
    "org.specs2"               %% "specs2-core"      % versions.specs2    % "test",
    "org.scalatest"            %% "scalatest"        % versions.scalatest % "test",
    "junit"                    %  "junit"            % versions.junit     % "test",
    "org.seleniumhq.selenium"  %  "selenium-java"    % versions.selenium % "test",
    "com.typesafe.akka"        %% "akka-testkit"     % versions.akka % "test",
    "com.typesafe.akka"        %% "akka-http-testkit"% versions.akkaHttp % "test",
    "org.webjars"              % "bootstrap"         % "3.3.7",
    "com.outr" %% "scribe" % versions.scribe,
    "com.outr" %% "scribe-slf4j" % versions.scribe,
    "nz.co.rossphillips" %% "scala-thumbnailer" % "0.5.SNAPSHOT"
  ))

  /** Dependencies only used by the JS project (note the use of %%% instead of %%) */
  val scalajsDependencies = Def.setting(Seq(
    "io.udash" %%% "udash-core-frontend" % versions.udash,
    "io.udash" %%% "udash-rpc-frontend" % versions.udash,
    "io.udash" %%% "udash-bootstrap" % versions.udash,
    "io.udash" %%% "udash-jquery" % versions.udashJQuery,
    "com.github.japgolly.scalacss" %%% "core" % versions.scalaCss,
    "com.github.japgolly.scalacss" %%% "ext-scalatags" % versions.scalaCss,
    "io.circe" %%% "circe-scalajs" % versions.circe,
    "com.lihaoyi" %% "utest" % "0.4.5" % "test",
    "org.scala-js" %%% "scalajs-dom" % "0.9.0"
  ))

  /** Dependencies for external JS libs that are bundled into a single .js file according to dependency order */
  val jsDependencies = Def.setting(Seq(
    //"org.webjars" % "bootstrap-sass" % versions.bootstrap / "3.3.1/javascripts/bootstrap.js" dependsOn "jquery.js"
  ))
}