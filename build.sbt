// If you are using Scala.js 0.6.x, you need the following import:
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}


/** codegen project containing the customized code generator */
lazy val codegen  = (project in file("codegen")).settings(
  name := "codegen",
  scalaVersion := Settings.versions.scala,
  libraryDependencies ++= Settings.codegenDependecies.value,
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("waveinch","maven"),
  resourceDirectory in Compile := baseDirectory.value / "../resources"
)

lazy val server: Project  = project
  .settings(
    name := "server",
    scalaVersion := Settings.versions.scala,
    scalaBinaryVersion := "2.12",
    scalacOptions ++= Settings.scalacOptionsServer,
    libraryDependencies ++= Settings.jvmDependencies.value,
    resolvers ++= Seq(Resolver.jcenterRepo, Resolver.bintrayRepo("hseeberger", "maven")),
    resolvers += Resolver.bintrayRepo("waveinch","maven"),
    slick := slickCodeGenTask.value , // register manual sbt command
    deleteSlick := deleteSlickTask.value,
    resourceDirectory in Compile := baseDirectory.value / "../resources",
    testOptions in Test += Tests.Argument(TestFrameworks.Specs2, "html"),
    mainClass in (Compile, packageBin) := Some("ch.wsl.box.rest.Boot"),
    mainClass in (Compile, run) := Some("ch.wsl.box.rest.Boot"),
    dockerExposedPorts ++= Seq(8080),
    packageName in Docker := "boxframework/box-framework",
    dockerUpdateLatest := true,
    dockerEntrypoint := Seq("/opt/docker/bin/boot","-Dconfig.file=/application.conf"),
    git.gitTagToVersionNumber := { tag:String =>
      Some(tag)
    },
    buildInfoKeys := Seq[BuildInfoKey](version),
    buildInfoPackage := "boxInfo",
    buildInfoObject := "BoxBuildInfo",
    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),
    compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
    WebKeys.packagePrefix in Assets := "public/",
    managedClasspath in Runtime += (packageBin in Assets).value,
    newrelicVersion := "5.14.0",
    newrelicIncludeApi := true
  )
  .enablePlugins(
    TomcatPlugin,
    JavaAppPackaging,
    DockerPlugin,
    GitVersioning,
    BuildInfoPlugin,
    SbtWeb,
    SbtTwirl,
    //NewRelic
  )
  //.aggregate(clients.map(projectToRef): _*)
  .dependsOn(sharedJVM)
  .dependsOn(codegen)


lazy val client: Project = (project in file("client"))
  .settings(
    name := "client",
    scalaVersion := Settings.versions.scala,
    scalacOptions ++= Settings.scalacOptions,
    resolvers += Resolver.jcenterRepo,
    resolvers += Resolver.bintrayRepo("waveinch","maven"),
    libraryDependencies ++= Settings.scalajsDependencies.value,
    jsDependencies ++= Settings.jsDependencies.value,
    // yes, we want to package JS dependencies
    skip in packageJSDependencies := false,
    // use Scala.js provided launcher code to start the client app
    scalaJSUseMainModuleInitializer := true,
    scalaJSUseMainModuleInitializer in Test := false,
    fork in fastOptJS := true,
    fork in fullOptJS := true,
    javaOptions in fastOptJS += "-Xmx4G -XX:MaxMetaspaceSize=1G -XX:MaxPermSize=1G -XX:+CMSClassUnloadingEnabled -Xss3m",
    javaOptions in fullOptJS += "-Xmx4G -XX:MaxMetaspaceSize=1G -XX:MaxPermSize=1G -XX:+CMSClassUnloadingEnabled -Xss3m",
    // use uTest framework for tests
//    testFrameworks += new TestFramework("utest.runner.Framework"),
//    // Compile tests to JS using fast-optimisation
//    scalaJSStage in Test := FastOptStage,
//    fullClasspath in Test ~= { _.filter(_.data.exists) },
//    //scalaJSOptimizerOptions ~= { _.withDisableOptimizer(true) },
//    compile := ((compile in Compile).dependsOn(compileStatics)).value,
//    compileStatics := {
//      IO.copyDirectory(sourceDirectory.value / "main/assets/fonts", crossTarget.value / StaticFilesDir / WebContent / "assets/fonts")
//      IO.copyDirectory(sourceDirectory.value / "main/assets/images", crossTarget.value / StaticFilesDir / WebContent / "assets/images")
//      val statics = compileStaticsForRelease.value
//      (crossTarget.value / StaticFilesDir).get
//    },
  )
  .enablePlugins(ScalaJSPlugin,ScalaJSWeb)
  .dependsOn(sharedJS)





//CrossProject is a Project compiled with both java and javascript
lazy val shared = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    name := "shared",
    scalaVersion := Settings.versions.scala,
    libraryDependencies ++= Settings.sharedJVMJSDependencies.value,
    resolvers += Resolver.jcenterRepo,
    resolvers += Resolver.bintrayRepo("waveinch","maven"),
  )
  .jsSettings(
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.0.0-RC5"
  )

lazy val sharedJVM: Project = shared.jvm.settings(name := "sharedJVM")

lazy val sharedJS: Project = shared.js.settings(name := "sharedJS")


// code generation task that calls the customized code generator
lazy val slick = taskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = Def.task{
  val dir = sourceDirectory.value
  val cp = (dependencyClasspath in Compile).value
  val s = streams.value
  val outputDir = (dir / "main" / "scala").getPath // place generated files in sbt's managed sources folder
  println(outputDir)
  runner.value.run("ch.wsl.box.codegen.CustomizedCodeGenerator", cp.files, Array(outputDir), s.log).failed foreach (sys error _.getMessage)
  val fname = outputDir + "/ch/wsl/box/generated/Entities.scala"
  val ffname = outputDir + "/ch/wsl/box/generated/FileTables.scala"
  val rname = outputDir + "/ch/wsl/box/generated/GeneratedRoutes.scala"
  val registryname = outputDir + "/ch/wsl/box/generated/EntityActionsRegistry.scala"
  val filename = outputDir + "/ch/wsl/box/generated/FileRoutes.scala"
  val regname = outputDir + "/ch/wsl/box/generated/GenRegistry.scala"
  Seq(file(fname),file(ffname),file(rname),file(registryname),file(filename),file(regname))    //include the generated files in the sbt project
}

lazy val deleteSlick = taskKey[Unit]("Delete slick generated files")
lazy val deleteSlickTask = Def.task{
  val dir = sourceDirectory.value
  val outputDir = (dir / "main" / "scala" / "ch" / "wsl" / "box" / "generated")
  IO.delete(Seq(
    outputDir
  ))
}

lazy val box = (project in file("."))
  .settings(
    boxDocker := boxDockerTask.value,
    boxPackage := boxTask.value,
    installBox := installBoxTask.value,
    dropBox := dropBoxTask.value,
  )

lazy val boxPackage = taskKey[Unit]("Package box")
lazy val boxTask = Def.sequential(
  (deleteSlick in server),
  (clean in client),
  (clean in server),
  (clean in codegen),
  (fullOptJS in Compile in client),
  (compile in Compile in codegen),
  (packageBin in Universal in server)
)


lazy val boxDocker = taskKey[Unit]("Create docker release")
lazy val boxDockerTask = Def.sequential(
  (deleteSlick in server),
  (clean in client),
  (clean in server),
  (clean in codegen),
  (fullOptJS in Compile in client),
  (compile in Compile in codegen),
  (publishLocal in Docker in server)
)

lazy val installBox = taskKey[Unit]("Install box schema")
lazy val installBoxTask = Def.sequential(
  //cleanAll,
  (compile in Compile in server).toTask,
  Def.task{
    (runMain in Compile in server).toTask(" ch.wsl.box.model.BuildBox").value
  }
)


lazy val dropBox = taskKey[Unit]("Drop box schema")
lazy val dropBoxTask = Def.sequential(
  //cleanAll,
  (compile in Compile in server).toTask,
  Def.task{
    (runMain in Compile in server).toTask(" ch.wsl.box.model.DropBox").value
  }
)




