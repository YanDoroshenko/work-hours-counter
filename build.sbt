name := "work-hours-counter"

version := "0.1"

scalaVersion := "2.12.2"

libraryDependencies ++=
  "com.typesafe.play" %% "play-json" % "2.6.0-M7" ::
    "org.scalafx" %% "scalafx" % "8.0.102-R11" ::
    Nil

mainClass in assembly := Some("com.github.yandoroshenko.workhourscounter.App")
assemblyOutputPath in assembly := file("target/" + name.value + "-" + version.value + ".jar")