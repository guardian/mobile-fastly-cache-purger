name := "mobile-fastly-cache-purger"

organization := "com.gu"

description:= "Lambda function which purges the Fastly cache from mobile front."

version := "1.0"

scalaVersion := "2.13.15"
val log4jVersion = "2.17.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-release:11",
  "-Ywarn-dead-code"
)

assemblyJarName := s"${name.value}.jar"

assembly / assemblyMergeStrategy := {
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case _ => MergeStrategy.first
}

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.3",
  "com.amazonaws" % "aws-lambda-java-events" % "3.14.0",
  "com.amazonaws" % "aws-java-sdk-sqs" % "1.12.774",
  "com.amazonaws" % "aws-java-sdk-sns" % "1.12.777",
  "com.amazonaws" % "aws-java-sdk-sts" % "1.12.774",
  "com.squareup.okhttp3" % "okhttp" % "4.12.0",
  "com.gu" %% "simple-configuration-ssm" % "2.0.0",
  "io.circe" %% "circe-parser" % "0.15.0-M1",
  "io.circe" %% "circe-core" % "0.15.0-M1",
  "io.circe" %% "circe-generic" % "0.15.0-M1",
  "com.gu" %% "fapi-client-play28" % "7.0.0",
  "org.slf4j" % "slf4j-api" % "2.0.16",
  "org.slf4j" % "slf4j-simple" % "2.0.16",
  "io.netty" % "netty-codec-http2" % "4.1.114.Final",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.18.0"
)
