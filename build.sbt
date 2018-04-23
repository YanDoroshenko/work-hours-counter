name := "work-hours-counter"

version := "1.2"

scalaVersion := "2.12.5"

libraryDependencies ++=
  "com.typesafe.play" %% "play-json" % "2.6.9" ::
    "org.scalafx" %% "scalafx" % "8.0.144-R12" ::
    "ch.qos.logback" % "logback-classic" % "1.2.3" ::
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0" ::
    Nil

mainClass in assembly := Some("com.github.yandoroshenko.workhourscounter.App")
assemblyOutputPath in assembly := file("target/" + name.value + "-" + version.value + ".jar")
