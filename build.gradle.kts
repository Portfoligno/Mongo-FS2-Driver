plugins {
  maven
  scala
  `java-library`
}
val scalaCompilerPlugin: Configuration = configurations.create("scalaCompilerPlugin")

tasks.getByName<Wrapper>("wrapper") {
  gradleVersion = "4.10.2"
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
  jcenter()
}
dependencies {
  scalaCompilerPlugin("org.scalamacros:paradise_2.12.7:2.1.1")
  api("org.scala-lang:scala-library:2.12.7")
  implementation("org.typelevel:spire-extras_2.12:0.16.0")

  implementation("co.fs2:fs2-reactive-streams_2.12:1.0.2") { setTransitive(false) }
  api("co.fs2:fs2-core_2.12:1.0.2")

  implementation("org.mongodb:mongodb-driver-reactivestreams:1.10.0")
  api("org.mongodb:bson:3.9.0")
}

tasks.withType<ScalaCompile> {
  scalaCompileOptions.additionalParameters = listOf(
      "-Xplugin:" + scalaCompilerPlugin.asPath,
      "-Ypartial-unification",
      "-language:higherKinds")
}
