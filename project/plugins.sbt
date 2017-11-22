addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.21")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.lihaoyi" % "workbench" % "0.3.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.5.3")

libraryDependencies += "org.scala-js" %% "scalajs-env-selenium" % "0.1.3"   //for tests


//uncomment to enable SASS compilation, you need sass
//CSS precompiler http://sass-lang.com/ used in client/src/main/assets/style.scss to import material design CSS lib
//addSbtPlugin("org.madoushi.sbt" % "sbt-sass" % "0.9.3")
