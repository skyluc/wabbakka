import com.typesafe.startscript.StartScriptPlugin

name := "wabbakka"

organization := "org.skyluc"

scalaVersion := "2.9.2"

libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.1"

libraryDependencies += "io.netty" % "netty" % "3.4.5.Final"

seq(StartScriptPlugin.startScriptForClassesSettings: _*)
