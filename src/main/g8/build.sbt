import Dependencies._

ThisBuild / organization := "$organization$"
ThisBuild / scalaVersion := "$scala_version$"
ThisBuild / version      := "1.0"

lazy val root = (project in file(".")).
  settings(
    name := "$name$",
    libraryDependencies ++= coreLibraries
  )
