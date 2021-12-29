addSbtPlugin("io.spray"         % "sbt-revolver"        % "0.9.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.8.1")
addSbtPlugin("org.scalameta"    % "sbt-scalafmt"        % "2.4.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-web"             % "1.4.4")

$if(use_scalajs_react.truthy)$
//scalajs
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.8.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("ch.epfl.scala"      % "sbt-web-scalajs-bundler"  % "0.20.0")
addSbtPlugin("com.typesafe.sbt"   % "sbt-gzip"                 % "1.0.2")
addSbtPlugin("com.typesafe.sbt"   % "sbt-digest"               % "1.1.3")
$endif$
