name := "mobile-fastly-cache-purger"

organization := "com.gu"

description:= "Lambda function which purges the Fastly cache from mobile front."

version := "1.0"

scalaVersion := "2.13.10"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:11",
  "-Ywarn-dead-code"
)

assemblyJarName := s"${name.value}.jar"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.2"
)
