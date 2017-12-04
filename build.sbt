name := "work-hours-counter"

version := "1.1"

scalaVersion := "2.12.4"

libraryDependencies ++=
  "com.typesafe.play" %% "play-json" % "2.6.7" ::
    "org.scalafx" %% "scalafx" % "8.0.144-R12" ::
    Nil

mainClass in assembly := Some("com.github.yandoroshenko.workhourscounter.App")
assemblyOutputPath in assembly := file("target/" + name.value + "-" + version.value + ".jar")
