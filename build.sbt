name := "mobile-fastly-cache-purger"

organization := "com.gu"

description:= "Lambda function which purges the Fastly cache from mobile front."

version := "1.0"

scalaVersion := "2.13.16"
val awsSdkVersion = "2.29.37"
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
  "com.amazonaws" % "aws-lambda-java-core" % "1.3.0",
  "com.amazonaws" % "aws-lambda-java-events" % "3.16.1",
  "software.amazon.awssdk" % "sts" % awsSdkVersion,
  "software.amazon.awssdk" % "s3" % awsSdkVersion,
  "com.squareup.okhttp3" % "okhttp" % "4.12.0",
  "com.gu" %% "simple-configuration-ssm" % "7.0.0",
  "io.circe" %% "circe-parser" % "0.15.0-M1",
  "io.circe" %% "circe-core" % "0.15.0-M1",
  "io.circe" %% "circe-generic" % "0.15.0-M1",
  "com.gu" %% "fapi-client-play30" % "28.0.1",
  "org.slf4j" % "slf4j-api" % "2.0.17",
  "org.slf4j" % "slf4j-simple" % "2.0.17",
  "com.fasterxml.jackson.core" % "jackson-core" % "2.19.4",
  "com.gu.etag-caching" %% "aws-s3-sdk-v2" % "7.0.0"
)
