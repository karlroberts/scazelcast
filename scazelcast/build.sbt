version       := "0.1"


scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

// dependency graphing
net.virtualvoid.sbt.graph.Plugin.graphSettings
