import net.moznion.sbt.spotless.config.{ GoogleJavaFormatConfig, JavaConfig }

inScope(Global) {
  Seq(
    githubPath := "gatling/gatling-enterprise-plugin-commons",
    gatlingDevelopers := Seq(
      GatlingDeveloper("tpetillot@gatling.io", "Thomas Petillot", isGatlingCorp = true)
    )
  )
}

val junitVersion = "5.8.2"
val okHttp3Version = "4.9.3" // SBT plugins cannot depend on an higher version, see: https://github.com/sbt/sbt/issues/5569

lazy val root = (project in file("."))
  .enablePlugins(GatlingOssPlugin)
  .settings(
    name := "gatling-enterprise-plugin-commons",
    crossPaths := false, // drop off Scala suffix from artifact names.
    autoScalaLibrary := false, // exclude scala-library from dependencies
    libraryDependencies ++= Seq(
      "org.junit.jupiter"    % "junit-jupiter-engine" % junitVersion                     % Test,
      "org.junit.jupiter"    % "junit-jupiter-api"    % junitVersion                     % Test,
      "net.aichler"          % "jupiter-interface"    % JupiterKeys.jupiterVersion.value % Test,
      "com.squareup.okhttp3" % "mockwebserver"        % okHttp3Version                   % Test,
      "com.squareup.okhttp3" % "okhttp"               % okHttp3Version
    ),
    spotlessJava := JavaConfig(
      googleJavaFormat = GoogleJavaFormatConfig()
    )
  )
