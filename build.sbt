scalaVersion := "2.13.1"

lazy val rootProject = project.in(file("./")).settings(
  name := "draftCaloriesCounter",
  version := "0.1",
  libraryDependencies ++= Dependencies.all,

  scalacOptions ++= Seq(
    "-encoding", "utf8", // Option and arguments on same line
    "-Xfatal-warnings",  // New lines for each options
    "-deprecation",
    "-unchecked",
    "-language:implicitConversions",
    "-language:higherKinds",
    "-language:existentials",
    "-language:postfixOps"
  )
)


