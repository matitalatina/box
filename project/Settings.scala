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
    val scala = "2.11.8"
    val ficus = "1.3.0"

    //HTTP actors
    val akka = "2.4.11"
    val akkaHttpJson = "1.10.1"
    val akkaCors = "0.1.7"

    //Testing
    val specs2 = "3.7"
    val junit = "4.12"
    val scalatest = "3.0.0"
    val selenium = "2.53.1"

    //logs
    val logback = "1.1.7"

    //json parsers
    val circe = "0.5.1"

    //database
    val postgres = "9.4.1211"
    val slick = "3.1.1"

    //frontend
    val scalaJSReact = "0.11.2"
    val scalaCss = "0.5.0"

    //js
    val reactJS = "15.3.2"
    val materialDesign = "1.2.1"

  }

  /**
    * These dependencies are shared between JS and JVM projects
    * the special %%% function selects the correct version for each project
    */
  val sharedJVMJSDependencies = Def.setting(Seq(
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
    "com.typesafe.akka"        %% "akka-http-experimental" % versions.akka,
    "de.heikoseeberger"        %% "akka-http-circe"  % versions.akkaHttpJson,
    "com.typesafe.akka"        %% "akka-actor"       % versions.akka,
    "ch.megard"                %% "akka-http-cors"   % versions.akkaCors,
    "io.circe"                 %% "circe-core"       % versions.circe,
    "io.circe"                 %% "circe-generic"    % versions.circe,
    "io.circe"                 %% "circe-parser"     % versions.circe,
    "ch.qos.logback"           %  "logback-classic"  % versions.logback,
    "org.specs2"               %% "specs2"           % versions.specs2    % "test",
    "junit"                    %  "junit"            % versions.junit     % "test",
    "org.seleniumhq.selenium"  %  "selenium-java"    % versions.selenium % "test"
  ))

  /** Dependencies only used by the JS project (note the use of %%% instead of %%) */
  val scalajsDependencies = Def.setting(Seq(
    "com.github.japgolly.scalajs-react" %%% "extra" % versions.scalaJSReact,
    "com.github.japgolly.scalacss" %%% "core" % versions.scalaCss,
    "com.github.japgolly.scalacss" %%% "ext-react" % versions.scalaCss,
    "io.circe" %%% "circe-core" % versions.circe,
    "io.circe" %%% "circe-generic" % versions.circe,
    "io.circe" %%% "circe-parser" % versions.circe,
    "io.circe" %%% "circe-scalajs" % versions.circe,
    "org.scalatest" %%% "scalatest" % versions.scalatest % "test",
    "io.github.widok" %%% "scala-js-momentjs" % "0.1.5"
  ))

  /** Dependencies for external JS libs that are bundled into a single .js file according to dependency order */
  val jsDependencies = Def.setting(Seq(
    "org.webjars.npm" % "react"     % versions.reactJS / "react-with-addons.js" commonJSName "React"    minified "react-with-addons.min.js",
    "org.webjars.npm" % "react-dom" % versions.reactJS / "react-dom.js"         commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js",
    "org.webjars.npm" % "material-design-lite" % versions.materialDesign / "dist/material.min.js"
  ))
}