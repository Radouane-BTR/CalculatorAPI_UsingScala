scalaVersion := "2.12.10" // Also supports 2.11.x

val http4sVersion = "0.20.16"

// Only necessary for SNAPSHOT releases
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  // Optional for auto-derivation of JSON codecs
  "io.circe" %% "circe-generic" % "0.10.0",
  // Optional for string interpolation to JSON model
  "io.circe" %% "circe-literal" % "0.10.0",

  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
)

libraryDependencies ++= Seq(

)

scalacOptions ++= Seq("-Ypartial-unification")

//addCompilerPlugin(
//  "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
//)

//libraryDependencies += "io.circe" %% "circe-parser" % circeVersion
libraryDependencies += "com.lihaoyi" %% "fastparse" % "2.2.2"
