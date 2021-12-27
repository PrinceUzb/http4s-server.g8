import Dependencies.Libraries._

object Dependencies {
  object Versions {
    val cats       = "$cats_version$"
    val catsEffect = "$cats_effect_version$"
    val circe      = "$circe_version$"
    val fs2        = "$fs2_version$"
    val http4s     = "$http4s_version$"
    val log4cats   = "$log4cats_version$"
    val skunk      = "$skunk_version$"
    val logback    = "$logback_version$"
    val ciris      = "$ciris_version$"
    val scalaCheck = "$scala_check_version$"
    val scalaTest  = "$scala_test_version$"
    val refined    = "$refined_version$"
    val tsec       = "$tsec_version$"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe" %% artifact % Versions.circe

    def skunk(artifact: String): ModuleID = "org.tpolecat" %% artifact % Versions.skunk

    def ciris(artifact: String): ModuleID = "is.cir" %% artifact % Versions.ciris

    def http4s(artifact: String): ModuleID = "org.http4s" %% artifact % Versions.http4s

    def refined(artifact: String): ModuleID = "eu.timepit" %% artifact % Versions.refined

    val circeCore    = circe("circe-core")
    val circeGeneric = circe("circe-generic")
    val circeParser  = circe("circe-parser")
    val circeRefined = circe("circe-refined")

    val skunkCore    = skunk("skunk-core")
    val skunkCirce   = skunk("skunk-circe")
    val skunkRefined = skunk("refined")

    val cirisCore    = ciris("ciris")
    val cirisRefined = ciris("ciris-refined")

    val http4sDsl    = http4s("http4s-dsl")
    val http4sCore   = http4s("http4s-core")
    val http4sServer = http4s("http4s-blaze-server")
    val http4sClient = http4s("http4s-blaze-client")
    val http4sCirce  = http4s("http4s-circe")
    val refinedType  = refined("refined")
    val refinedCats  = refined("refined-cats")

    val tsecHttp4s = "io.github.jmcardon" %% "tsec-http4s" % Versions.tsec
    val cats       = "org.typelevel"      %% "cats-core"   % Versions.cats
    val catsEffect = "org.typelevel"      %% "cats-effect" % Versions.catsEffect
    val fs2        = "co.fs2"             %% "fs2-core"    % Versions.fs2

    val log4cats = "org.typelevel" %% "log4cats-slf4j"  % Versions.log4cats
    val logback  = "ch.qos.logback" % "logback-classic" % Versions.logback

    // Test
    val scalaCheck = "org.scalacheck" %% "scalacheck" % Versions.scalaCheck
    val scalaTest  = "org.scalatest"  %% "scalatest"  % Versions.scalaTest

  }

  val circeLibs = Seq(circeCore, circeGeneric, circeParser, circeRefined)

  val catsLibs = Seq(cats, catsEffect)

  val http4sLibs = Seq(http4sDsl, http4sCore, http4sServer, http4sClient, http4sCirce)

  val cirisLibs = Seq(cirisRefined, cirisCore)

  val logLibs = Seq(log4cats, logback)

  val coreLibraries: Seq[ModuleID] = catsLibs ++ cirisLibs ++ circeLibs ++ http4sLibs ++ logLibs ++ Seq(
    skunkCore,
    skunkCirce,
    skunkRefined,
    fs2,
    refinedType,
    tsecHttp4s
  )

  val testLibraries = Seq(
    scalaCheck,
    scalaTest
  )
}
