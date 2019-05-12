name := "Akka-Http-Learning"

version := "0.1"

scalaVersion := "2.12.8"

val `akka-http` = "com.typesafe.akka" %% "akka-http" % "10.1.8"
val `akka-stream` = "com.typesafe.akka" %% "akka-stream" % "2.5.19"
val `akka-http-spray-json` = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"
val `akka-http-stream-testkit` = "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19"
val `akka-http-testkit` = "com.typesafe.akka" %% "akka-http-testkit" % "10.1.8"
val scalatest = "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % Test
val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
val cats = "org.typelevel" %% "cats-core" % "2.0.0-M1"
val catsEffects = "org.typelevel" %% "cats-effect" % "2.0.0-M1"

libraryDependencies ++= Seq(
  `akka-http`,
  `akka-http-stream-testkit`,
  `akka-http-testkit`,
  `akka-stream`,
  `akka-http-spray-json`,
  scalatest,
  scalacheck,
  cats,
  catsEffects
)
