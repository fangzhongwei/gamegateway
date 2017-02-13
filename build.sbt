name := "gamegateway"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

lazy val akkaVersion = "2.4.14"

// scalaz-bintray resolver needed for specs2 library
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies += ws

libraryDependencies += "org.webjars" % "flot" % "0.8.3"
libraryDependencies += "org.webjars" % "bootstrap" % "3.3.6"

libraryDependencies += "com.jxjxgo.common" % "common-utils_2.11" % "1.0"
libraryDependencies += "com.jxjxgo.common" % "common-redis_2.11" % "1.0"
libraryDependencies += "com.jxjxgo.common" % "common-rabbitmq_2.11" % "1.0"
libraryDependencies += "com.jxjxgo.sms" % "smscommon_2.11" % "1.0"
libraryDependencies += "com.jxjxgo.sso" % "ssocommonlib_2.11" % "1.0"
libraryDependencies += "com.jxjxgo.common" % "common-edecrypt_2.11" % "1.0"
libraryDependencies += "com.jxjxgo.gamegateway" % "gamegatewaycommonlib_2.11" % "1.0"
