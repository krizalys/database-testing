lazy val root = (project in file("."))
    .settings(
        name := "Test",
        version := "1.0",
        scalaVersion := "2.12.1",
        organization := "com.krizalys",
        libraryDependencies += "net.jcazevedo" %% "moultingyaml" % "0.4.0",
        libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test,
        libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test
    )
