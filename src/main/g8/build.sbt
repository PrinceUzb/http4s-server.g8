// give the user a nice default project!
ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.6"

lazy val root = (project in file(".")).
  settings(
    name := "http4s-server"
  )
