import BuildSettings.versionV
import Resolvers._

resolvers += sprayRepo

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.0"
  Seq(
    "com.owtelse"       %%   "scazelcast-api"      % versionV,
    "com.typesafe.akka"   %%  "akka-actor"     % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV    % "test"
  )
}

net.virtualvoid.sbt.graph.Plugin.graphSettings

