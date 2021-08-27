import net.moznion.sbt.spotless.config.{ GoogleJavaFormatConfig, JavaConfig }

inScope(Global) {
  Seq(
    githubPath := "gatling/gatling-enterprise-plugin-commons",
    gatlingDevelopers := Seq(
      GatlingDeveloper("tpetillot@gatling.io", "Thomas Petillot", isGatlingCorp = true)
    )
  )
}

val junitVersion = "5.7.2"
val okHttp3Version = "4.9.1"

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
