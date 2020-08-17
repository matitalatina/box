addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.11-0.6")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.33")

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.0.2")


addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.5.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"  % "1.0.0")


addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")

resolvers += Resolver.bintrayRepo("oyvindberg", "converter")

addSbtPlugin("org.scalablytyped.converter" % "sbt-converter06" % "1.0.0-beta24")
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler-sjs06" % "0.18.0")
