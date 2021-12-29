import Dependencies._

ThisBuild / organization := "$organization$"
ThisBuild / scalaVersion := "$scala_version$"
ThisBuild / version      := "1.0"

lazy val server = (project in file("modules/server"))
  .settings(
    name := "$name$",
    libraryDependencies ++= coreLibraries
  )$if(use_scalajs_react.truthy)$
  .settings(
    scalaJSProjects         := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    pipelineStages          := Seq(digest, gzip),
    Compile / compile       := ((Compile / compile) dependsOn scalaJSPipeline).value)
  .enablePlugins(WebScalaJSBundlerPlugin)
  $else$
  .enablePlugins(SbtWeb)
  $endif$


lazy val tests = project
  .in(file("modules/tests"))
  .configs(IntegrationTest)
  .settings(
    name := "$name$-test-suite",
    Defaults.itSettings,
    libraryDependencies ++= testLibraries ++ testLibraries.map(_ % Test)
  )
  .dependsOn(server)

$if(use_scalajs_react.truthy)$
lazy val client = (project in file("modules/client"))
  .settings(
    name := "client",
    scalaJSUseMainModuleInitializer := true,
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
      "io.github.chronoscala"             %%% "chronoscala"   % "2.0.2",
      "com.github.japgolly.scalajs-react" %%% "core"          % Versions.scalaJsReact,
      "com.github.japgolly.scalajs-react" %%% "extra"         % Versions.scalaJsReact,
      "com.github.japgolly.scalacss"      %%% "ext-react"     % Versions.scalaCss,
      "io.circe"                          %%% "circe-core"    % Versions.circe,
      "io.circe"                          %%% "circe-parser"  % Versions.circe,
      "io.circe"                          %%% "circe-generic" % Versions.circe,
      "io.circe"                          %%% "circe-refined" % Versions.circe,
      "eu.timepit"                        %%% "refined"       % Versions.refined
    ),
    webpackEmitSourceMaps := false,
    Compile / npmDependencies ++= Seq(
      "react" -> Versions.reactJs,
      "react-dom" -> Versions.reactJs
    )
  )
  .enablePlugins(ScalaJSBundlerPlugin)
$endif$