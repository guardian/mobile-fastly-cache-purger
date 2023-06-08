name := "mobile-fastly-cache-purger"

organization := "com.gu"

description:= "Lambda function which purges the Fastly cache from mobile front."

version := "1.0"

scalaVersion := "2.13.10"
val log4jVersion = "2.17.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:11",
  "-Ywarn-dead-code"
)

assemblyJarName := s"${name.value}.jar"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.2",
  "com.amazonaws" % "aws-lambda-java-events" % "3.11.0",
  "com.squareup.okhttp3" % "okhttp" % "4.9.2"

)
