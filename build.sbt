import net.moznion.sbt.spotless.config.{ GoogleJavaFormatConfig, JavaConfig, SpotlessConfig }

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

inScope(Global) {
  Seq(
    githubPath := "gatling/gatling-enterprise-plugin-commons",
    gatlingDevelopers := Seq(
      GatlingDeveloper("tpetillot@gatling.io", "Thomas Petillot", isGatlingCorp = true)
    )
  )
}

val junitVersion = "5.8.2"

lazy val root = (project in file("."))
  .enablePlugins(GatlingOssPlugin)
  .settings(
    name := "gatling-enterprise-plugin-commons",
    crossPaths := false, // drop off Scala suffix from artifact names.
    autoScalaLibrary := false, // exclude scala-library from dependencies
    libraryDependencies ++= Seq(
      "org.junit.jupiter"          % "junit-jupiter-engine" % junitVersion                     % Test,
      "org.junit.jupiter"          % "junit-jupiter-api"    % junitVersion                     % Test,
      "net.aichler"                % "jupiter-interface"    % JupiterKeys.jupiterVersion.value % Test,
      "com.squareup.okhttp3"       % "mockwebserver"        % "4.9.3"                          % Test,
      "com.fasterxml.jackson.core" % "jackson-databind"     % "2.13.3",
      "io.gatling"                 % "gatling-scanner"      % "1.1.0"
    ),
    spotlessJava := JavaConfig(
      googleJavaFormat = GoogleJavaFormatConfig()
    ),
    spotless := SpotlessConfig(
      applyOnCompile = !sys.env.getOrElse("CI", "false").toBoolean
    )
  )
