
//uncomment to enable SASS
//import com.typesafe.sbt.web.SbtWeb

import UDashBuild._
import org.scalajs.sbtplugin.cross.CrossProject



/** codegen project containing the customized code generator */
lazy val codegen  = (project in file("codegen")).settings(
  name := "codegen",
  scalaVersion := Settings.versions.scala,
  libraryDependencies ++= Settings.codegenDependecies.value,
  resolvers += Resolver.jcenterRepo,
  resourceDirectory in Compile := baseDirectory.value / "../resources"
)

lazy val server: Project  = (project in file("server"))
  .settings(
    name := "server",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    //resolvers += "nightlies" at "https://scala-ci.typesafe.com/artifactory/scala-release-temp/",
    //scalaVersion := "2.12.5-bin-2791989",
    scalaBinaryVersion := "2.12",
    scalacOptions ++= Settings.scalacOptionsServer,
    libraryDependencies ++= Settings.jvmDependencies.value,
    resolvers += Resolver.jcenterRepo,
    slick <<= slickCodeGenTask, // register manual sbt command
    sourceGenerators in Compile <+= slickCodeGenTask, // register automatic code generation on every compile, comment this line for only manual use
    resourceDirectory in Compile := baseDirectory.value / "../resources",
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "html")
  )
  .enablePlugins(TomcatPlugin)
  .enablePlugins(JavaAppPackaging)
  //.aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)
  .dependsOn(codegen)

lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    version := Settings.version,
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    libraryDependencies ++= Settings.scalajsDependencies.value,
    jsDependencies ++= Settings.jsDependencies.value,
    // RuntimeDOM is needed for tests
    jsDependencies += RuntimeDOM % "test",
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
    // use Scala.js provided launcher code to start the client app
    persistLauncher := true,
    persistLauncher in Test := false,
    fork in fastOptJS := true,
    fork in fullOptJS := true,
    javaOptions in fastOptJS += "-Xmx4G -XX:MaxMetaspaceSize=1G -XX:MaxPermSize=1G -XX:+CMSClassUnloadingEnabled -Xss3m",
    javaOptions in fullOptJS += "-Xmx4G -XX:MaxMetaspaceSize=1G -XX:MaxPermSize=1G -XX:+CMSClassUnloadingEnabled -Xss3m",
    // use uTest framework for tests
    testFrameworks += new TestFramework("utest.runner.Framework"),
    //jsEnv in Test := new org.scalajs.jsenv.selenium.SeleniumJSEnv(org.scalajs.jsenv.selenium.Chrome()),
    requiresDOM := true,
    // Compile tests to JS using fast-optimisation
    scalaJSStage in Test := FastOptStage,
    fullClasspath in Test ~= { _.filter(_.data.exists) },
    //scalaJSOptimizerOptions ~= { _.withDisableOptimizer(true) },
    compile <<= (compile in Compile).dependsOn(compileStatics),
    compileStatics := {
      IO.copyDirectory(sourceDirectory.value / "main/assets/fonts", crossTarget.value / StaticFilesDir / WebContent / "assets/fonts")
      IO.copyDirectory(sourceDirectory.value / "main/assets/images", crossTarget.value / StaticFilesDir / WebContent / "assets/images")
      val statics = compileStaticsForRelease.value
      (crossTarget.value / StaticFilesDir).***.get
    },
    //      artifactPath in(Compile, fastOptJS) :=
    //        (crossTarget in(Compile, fastOptJS)).value / StaticFilesDir / WebContent / "scripts" / "frontend-impl-fast.js",
    //      artifactPath in(Compile, fullOptJS) :=
    //        (crossTarget in(Compile, fullOptJS)).value / StaticFilesDir / WebContent / "scripts" / "frontend-impl.js",
    artifactPath in(Compile, packageJSDependencies) :=
      (crossTarget in(Compile, packageJSDependencies)).value / "client-jsdeps.js",
    artifactPath in(Compile, packageMinifiedJSDependencies) :=
      (crossTarget in(Compile, packageMinifiedJSDependencies)).value / "client-jsdeps.js"
    //      artifactPath in(Compile, packageScalaJSLauncher) :=
    //        (crossTarget in(Compile, packageScalaJSLauncher)).value / StaticFilesDir / WebContent / "scripts" / "frontend-init.js"
  )
  .enablePlugins(ScalaJSPlugin)
  //    .enablePlugins(WorkbenchPlugin)
  //    .settings(workbenchSettings:_*)
  //    .settings(
  //      bootSnippet := "ch.wsl.box.client.Init().main();",
  //      updatedJS := {
  //        var files: List[String] = Nil
  //        ((crossTarget in Compile).value / StaticFilesDir ** "*.js").get.foreach {
  //          (x: File) =>
  //            streams.value.log.info("workbench: Checking " + x.getName)
  //            FileFunction.cached(streams.value.cacheDirectory / x.getName, FilesInfo.lastModified, FilesInfo.lastModified) {
  //              (f: Set[File]) =>
  //                val fsPath = f.head.getAbsolutePath.drop(new File("").getAbsolutePath.length)
  //                files = "http://localhost:12345" + fsPath :: files
  //                f
  //            }(Set(x))
  //        }
  //        files
  //      },
  //      //// use either refreshBrowsers OR updateBrowsers
  //      // refreshBrowsers <<= refreshBrowsers triggeredBy (compileStatics in Compile)
  //      updateBrowsers <<= updateBrowsers triggeredBy (compileStatics in Compile)
  //    )
  //.enablePlugins(SbtWeb) uncomment to enable SASS
  .dependsOn(sharedJS)


lazy val root: Project = (project in file("."))
  .settings(
    serve := Def.sequential(
        cleanUi,
        //cleanAll.value,
        (fastOptJS in Compile in client).toTask,
        copyUiFilesDev,
        (run in Compile in server).toTask("")
      ).value,
      cleanAll := {
        (clean in Compile in client).toTask.value
        (clean in Compile in server).toTask.value
      },
      box := Def.sequential(
        cleanUi,
        cleanAll,
        (fullOptJS in Compile in client),
        copyUiFiles,
        (packageBin in Universal in server)
      ).value
  )

// Client projects (just one in this case)
lazy val clients = Seq(client)


//CrossProject is a Project compiled with both java and javascript
lazy val shared: CrossProject = (crossProject.crossType(CrossType.Pure) in file("shared"))
  .settings(
    name := "shared",
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Settings.sharedJVMJSDependencies.value
  )

lazy val sharedJVM: Project = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS: Project = shared.js.settings(name := "sharedJS")



lazy val box = taskKey[Unit]("Package box")

lazy val serve = taskKey[Unit]("start server")
lazy val cleanAll = taskKey[Unit]("clean all projects")


// code generation task that calls the customized code generator
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams).map { (dir, cp, r, s) =>
  val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
  toError(r.run("ch.wsl.box.codegen.CustomizedCodeGenerator", cp.files, Array(outputDir), s.log))
  val fname = outputDir + "/ch/wsl/box/model/Entities.scala"
  val ffname = outputDir + "/ch/wsl/box/model/FileTables.scala"
  val rname = outputDir + "/ch/wsl/box/rest/routes/GeneratedRoutes.scala"
  val registryname = outputDir + "/ch/wsl/box/model/EntityActionsRegistry.scala"
  val filename = outputDir + "/ch/wsl/box/rest/routes/FileRoutes.scala"
  Seq(file(fname),file(ffname),file(rname),file(registryname),file(filename))    //include the generated files in the sbt project
}

lazy val copyUiFiles = Def.task{
  val mappings: Seq[(File,File)] = Seq(
    file("client/target/scala-2.12/client-jsdeps.js") -> file("resources/js/deps.js"),
    file("client/target/scala-2.12/client-opt.js") -> file("resources/js/app.js"),
    file("client/target/scala-2.12/client-launcher.js") -> file("resources/js/launcher.js")
  )
  IO.copy(mappings)
}

lazy val cleanUi = Def.task{
  IO.delete(Seq(
    file("resources/js/deps.js"),
    file("resources/js/app.js"),
    file("resources/js/launcher.js")
  ))
}

lazy val copyUiFilesDev = Def.task{
  val mappings: Seq[(File,File)] = Seq(
    file("client/target/scala-2.12/client-jsdeps.js") -> file("resources/js/deps.js"),
    file("client/target/scala-2.12/client-fastopt.js") -> file("resources/js/app.js"),
    file("client/target/scala-2.12/client-fastopt.js.map") -> file("resources/js/app.js.map"),
    file("client/target/scala-2.12/client-launcher.js") -> file("resources/js/launcher.js")
  )
  IO.copy(mappings)
}



