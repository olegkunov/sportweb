import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.sportweb",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "SportWeb",
    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= Seq(akkaHttp, h2, slick)
  )
