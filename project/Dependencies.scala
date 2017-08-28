import sbt._

object Dependencies {

  lazy val akkaHttp             = "com.typesafe.akka"           %% "akka-http"  % "10.0.9"

  lazy val akkaHttpSprayJson    = "com.typesafe.akka"   %% "akka-http-spray-json"  % "10.0.9"

  lazy val slick                = "com.typesafe.slick"  %% "slick"      % "3.2.1"

  lazy val h2                   = "com.h2database"      % "h2"          % "1.4.196"

  lazy val scalaTest            = "org.scalatest"      %% "scalatest"  % "3.0.3"

}
