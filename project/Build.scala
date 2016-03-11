
import com.typesafe.sbt.web.SbtWeb
import sbt._
import Keys._
import Tests._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Project.projectToRef


object stagedBuild extends Build {

  lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared"))
    .settings(
      scalaVersion := Settings.versions.scala,
      libraryDependencies ++= Settings.sharedJVMJSDependencies.value
    )

  lazy val sharedJVM = shared.jvm.settings(name := "sharedJVM")

  lazy val sharedJS = shared.js.settings(name := "sharedJS")

  // use eliding to drop some debug code in the production build
  lazy val elideOptions = settingKey[Seq[String]]("Set limit for elidable functions")

  lazy val server = (project in file("server"))
    .settings(
      name := "server",
      version := Settings.version,
      scalaVersion := Settings.versions.scala,
      scalacOptions ++= Settings.scalacOptionsServer,
      libraryDependencies ++= Settings.jvmDependencies.value,
      resolvers += Resolver.jcenterRepo,
      slick <<= slickCodeGenTask, // register manual sbt command
      sourceGenerators in Compile <+= slickCodeGenTask, // register automatic code generation on every compile, remove for only manual use
      resourceDirectory in Compile := baseDirectory.value / "../resources",
      testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "html")
    )
    .aggregate(clients.map(projectToRef): _*)
    .dependsOn(sharedJVM)
    .dependsOn(codegenProject)


  lazy val client: Project = (project in file("client"))
    .settings(
      name := "client",
      version := Settings.version,
      scalaVersion := Settings.versions.scala,
      scalacOptions ++= Settings.scalacOptions,
      libraryDependencies ++= Settings.scalajsDependencies.value,
      // by default we do development build, no eliding
      elideOptions := Seq(),
      scalacOptions ++= elideOptions.value,
      jsDependencies ++= Settings.jsDependencies.value,
      // RuntimeDOM is needed for tests
      jsDependencies += RuntimeDOM % "test",
      // yes, we want to package JS dependencies
      skip in packageJSDependencies := false,
      // use Scala.js provided launcher code to start the client app
      persistLauncher := true,
      persistLauncher in Test := false,
      // use uTest framework for tests
      testFrameworks += new TestFramework("utest.runner.Framework"),
      requiresDOM := true,
      // Compile tests to JS using fast-optimisation
      scalaJSStage in Test := FastOptStage,
      fullClasspath in Test ~= { _.filter(_.data.exists) },
      // copy  javascript files to js folder,that are generated using fastOptJS/fullOptJS
      crossTarget in (Compile, fullOptJS) := file("js"),
      crossTarget in (Compile, fastOptJS) := file("js"),
      crossTarget in (Compile, packageJSDependencies) := file("js"),
      crossTarget in (Compile, packageScalaJSLauncher) := file("js"),
      crossTarget in (Compile, packageMinifiedJSDependencies) := file("js"),
      artifactPath in (Compile, fastOptJS) := ((crossTarget in (Compile, fastOptJS)).value / ((moduleName in fastOptJS).value + "-opt.js"))
    )
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(SbtWeb)
    .dependsOn(sharedJS)


  // Client projects (just one in this case)
  lazy val clients = Seq(client)

  /** codegen project containing the customized code generator */
  lazy val codegenProject = (project in file("codegen")).settings(
      name := "codegen",
      scalaVersion := Settings.versions.scala,
      libraryDependencies ++= Settings.codegenDependecies.value,
      resolvers += Resolver.jcenterRepo,
      resourceDirectory in Compile := baseDirectory.value / "../resources"
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