
import sbt._
import Keys._
import Tests._

/** 
 *  This is a slightly more advanced sbt setup using two projects.
 *  The first one, "codegen" a customized version of Slick's
 *  code-generator. The second one "main" depends on "codegen", which means
 *  it is compiled after "codegen". "main" uses the customized
 *  code-generator from project "codegen" as a sourceGenerator, which is run
 *  to generate Slick code, before the code in project "main" is compiled.
 */
object stagedBuild extends Build {
  /** main project containing main source code depending on slick and codegen project */
  lazy val mainProject = Project(
    id="main",
    base=file("."),
    settings = sharedSettings ++ Seq(
      javaOptions in run += "-Xmx2G",
      organization := "ch.wsl",
      name := "postgres-restify",
      //scalaOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-feature"),
      libraryDependencies ++= List(
        "io.spray"                 %% "spray-can"        % "1.3.3",
        "io.spray"                 %% "spray-routing"    % "1.3.3",
        "io.spray"                 %% "spray-testkit"    % "1.3.3",
        "com.typesafe.akka"        %% "akka-actor"       % "2.3.9",
        "org.specs2"               %% "specs2"           % "2.3.11"    % "test",
        //"com.github.tminglei"      %% "slick-pg"         % "0.8.2",
        "junit"                    %  "junit"            % "4.8.1"     % "test",
        "ch.qos.logback"           %  "logback-classic"  % "1.1.1",
        "org.scalatest"            %  "scalatest_2.11"   % "2.1.5",
        "org.seleniumhq.selenium"  %  "selenium-java"    % "2.28.0" % "test",
        "org.json4s"               %% "json4s-native"    % "3.3.0"

      ),
      unmanagedResourceDirectories in Compile <++= baseDirectory { base =>
        Seq( base / "src/main/webapp" )
      },
      resolvers ++= Seq(
        "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/",
        "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
        "typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
        "spray repo" at "http://repo.spray.io/"
      ),
      slick <<= slickCodeGenTask, // register manual sbt command
      sourceGenerators in Compile <+= slickCodeGenTask, // register automatic code generation on every compile, remove for only manual use
      resourceDirectory in Compile := baseDirectory.value / "resources",
      scalacOptions in Test ++= Seq("-Yrangepos"),
      testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "html")//,
      //resourceDirectory in Test := baseDirectory.value / "src" / "test" / "resources"
    )
  ).dependsOn( codegenProject )
  /** codegen project containing the customized code generator */
  lazy val codegenProject = Project(
    id="codegen",
    base=file("codegen"),
    settings = sharedSettings ++ Seq(
      libraryDependencies ++= List(
        "com.typesafe.slick" %% "slick-codegen" % "3.1.1"
      ),
      resourceDirectory in Compile := baseDirectory.value / "../resources"
    )
  )
  
  // shared sbt config between main project and codegen project
  val sharedSettings = Project.defaultSettings ++ Seq(
    scalaVersion := "2.11.7",
    libraryDependencies ++= List(
      "com.typesafe.slick" %% "slick" % "3.1.1",
      "postgresql"               %  "postgresql"       % "9.1-901.jdbc4",
      "net.ceedubs"              %% "ficus"             % "1.1.2"

    )
  )

  // code generation task that calls the customized code generator
  lazy val slick = TaskKey[Seq[File]]("gen-tables")
  lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
    val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
    toError(r.run("ch.wsl.codegen.CustomizedCodeGenerator", cp.files, Array(outputDir), s.log))
    val fname = outputDir + "/ch/wsl/model/Tables.scala"
    val rname = outputDir + "/ch/wsl/rest/service/GeneratedRoutes.scala"
    Seq(file(fname),file(rname))
  }
}