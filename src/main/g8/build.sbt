import Dependencies._

ThisBuild / organization := "uz"
ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version      := "1.0"

lazy val root = (project in file("."))
  .settings(
    name := "soccer"
  )
  .aggregate(server, tests)

lazy val server = (project in file("modules/server"))
  .settings(
    name := "soccer",
    libraryDependencies ++= coreLibraries,
    scalacOptions ++= CompilerOptions.cOptions,
    coverageEnabled := true
  )

lazy val tests = project
  .in(file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "soccer-test-suite",
    testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
    Defaults.itSettings,
    scalacOptions ++= CompilerOptions.cOptions,
    libraryDependencies ++= testLibraries
  )
  .dependsOn(server)

val runTests = inputKey[Unit]("Runs tests")
val runServer = inputKey[Unit]("Runs server")

runServer := {
  (server / Compile / run).evaluated
}

runTests := {
  (tests / Test / test).value
}