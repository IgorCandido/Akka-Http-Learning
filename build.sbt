name := "Akka-Http-Learning"

version := "0.1"

scalaVersion := "2.12.8"

val `akka-http` = "com.typesafe.akka" %% "akka-http"   % "10.1.8"
val `akka-stream` = "com.typesafe.akka" %% "akka-stream" % "2.5.19"
val `akka-http-spray-json` = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"

libraryDependencies ++= Seq(`akka-http`, `akka-stream`, `akka-http-spray-json`)