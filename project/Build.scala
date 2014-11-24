import sbt._
import Keys._



object BuildSettings {
  import Resolvers._

  val versionV = "0.2-SNAPSHOT"

  val paradiseVersion = "2.0.0"

  val hazelcastV = "3.2.1"

  val nscalaV = "1.0.0"

  val coreDeps = Seq(
    "org.scalaz" %% "scalaz-core" % "7.0.3",
    "com.hazelcast"       %   "hazelcast"      % hazelcastV withSources() withJavadoc(),
    "com.github.nscala-time" %% "nscala-time" % nscalaV,
    "org.specs2"          %%  "specs2-core"   % "2.3.9"  % "test" withSources() withJavadoc(),
    "org.scalacheck"      %%  "scalacheck"    % "1.11.3" % "test" withSources() withJavadoc()
  )

  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.owtelse",
    version := versionV,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
    scalaVersion := "2.10.4",
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4"), //, "2.11.0", "2.11.1"),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    resolvers += Resolver.sonatypeRepo("releases"),
    addCompilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full),
    libraryDependencies ++= coreDeps
  )
}

object MyBuild extends Build {
  import BuildSettings._

  lazy val root: Project = Project(
    "scazelcast-all",
    file("."),
    settings = buildSettings ++ Seq(
      run <<= run in Compile in scazelcastApi
    )
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
