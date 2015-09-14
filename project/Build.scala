import com.amazonaws.services.s3.model.Region
import com.ambiata.promulgate.project.ProjectPlugin.promulgate
import ohnosequences.sbt.SbtS3Resolver.{S3Resolver, s3 => ss33, _}
import sbt.Keys._
import sbt._

object ProjectSettings {
  val organisation = "com.owtelse"
  val isSnapshot = true
  private[this] val versionNum = "0.2.2"
  val version = if(isSnapshot) versionNum else versionNum + "-SNAPSHOT"

}

object Versions {
  val nscalaV = "1.2.0"
  val scalazV = "7.1.0"

  val specs2V = "2.4.1"
  val scalacheckV = "1.11.3"
  val paradiseVersion = "2.1.0-M5"
  val hazelcastV = "3.2.4"
}

object BuildSettings {
  import Versions._

  val versionV = "0.2-SNAPSHOT"

  val coreDeps = Seq(
    "org.scalaz" %% "scalaz-core" % scalazV withSources() withJavadoc(),
    "com.hazelcast"       %   "hazelcast"      % hazelcastV withSources() withJavadoc(),
    "com.github.nscala-time" %% "nscala-time" % nscalaV,
    "org.specs2"          %%  "specs2-core"   % specs2V  % "test" withSources() withJavadoc(),
    "org.scalacheck"      %%  "scalacheck"    % scalacheckV % "test" withSources() withJavadoc()
  )

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := ProjectSettings.organisation,
    version in ThisBuild := ProjectSettings.version,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    scalaVersion := "2.11.6",
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full),
    libraryDependencies ++= coreDeps
  ) ++ S3Resolver.defaults ++ Seq(
    publishMavenStyle           := false
    , publishArtifact in Test     := false
    , pomIncludeRepository        := { _ => false }
    , publishTo                   <<= (s3credentials).apply((creds) =>
      Some(S3Resolver(creds, false, Region.AP_Sydney)("owtelse-oss-publish", ss33("owtelse-repo-oss")).withIvyPatterns))
  )
}

object MyBuild extends Build {
  import BuildSettings._
  import Versions._

  lazy val root: Project = Project(
    "scazelcast-all",
    file("."),
    settings = buildSettings ++ Seq(
      run <<= run in Compile in scazelcastApi
    )  ++ promulgate.library("com.owtelse", "owtelse-repo-oss")
  ) aggregate(macros, scazelcastApi, scazelcastAkka, scazelcastDemo)

  lazy val macros: Project = Project(
    "macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies ++= (
        if (scalaVersion.value.startsWith("2.10")) List("org.scalamacros" %% "quasiquotes" % paradiseVersion)
        else Nil
      )
    )
  )

  lazy val scazelcastApi: Project = Project(
    "scazelcast-api",
    file("scazelcast-api"),
    settings = buildSettings
  ) dependsOn(macros)

  lazy val scazelcastAkka: Project = Project(
    "scazelcast-akka",
    file("scazelcast-akka"),
    settings = buildSettings
  ) dependsOn(macros, scazelcastApi)


  lazy val scazelcastDemo: Project = Project(
    "scazelcast-demo",
    file("scazelcast-demo"),
    settings = buildSettings
  ) dependsOn(macros, scazelcastAkka)
}
