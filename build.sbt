import sbt.Keys.libraryDependencies

val scala3Version = "3.7.3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-3-Test",
    version := "0.1.0",

    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked"
    ),

    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3"
    )
  )

