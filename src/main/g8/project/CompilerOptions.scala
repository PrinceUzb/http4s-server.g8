object CompilerOptions {
  val cOptions = Seq(
    "-Ymacro-annotations",
    "-encoding",
    "utf8",             // Option and arguments on same line
    "-Xfatal-warnings", // New lines for each options
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps",
    "-Yrangepos",
    "-Wconf:cat=unused:info"
  )
}
