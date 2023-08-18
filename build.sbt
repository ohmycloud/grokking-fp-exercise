lazy val root = (project in file("."))
  .settings(
    name := "grokkingfp-io",
    organization := "ohmycloudy",
    version := "1.0",
    scalaVersion := "3.3.0",
    scalacOptions ++= List("-unchecked", "-deprecation", "-explain"),
    libraryDependencies ++= Seq(
        "org.typelevel"     %% "cats-effect"      % "3.5.1",
        "org.typelevel"     %% "cats-effect"      % "3.4.10",
        "co.fs2"            %% "fs2-core"         % "3.8.0",
        "org.scalatest"     %% "scalatest"        % "3.2.16"   % Test,
        "org.scalatestplus" %% "scalacheck-1-16"  % "3.2.14.0" % Test,
        // imperative libraries:
        "com.typesafe.akka"  % "akka-actor_2.13"  % "2.6.20",
        "org.apache.jena"    % "apache-jena-libs" % "4.9.0",
        "org.apache.jena"    % "jena-fuseki-main" % "4.9.0",
        "org.slf4j"          % "slf4j-nop"        % "2.0.7"
    ),
    initialCommands := s"""
      import fs2._, cats.effect._, cats.implicits._, cats.effect.unsafe.implicits.global
      import scala.concurrent.duration._, java.util.concurrent._
      import scala.jdk.javaapi.CollectionConverters.asScala
      import org.apache.jena.query._, org.apache.jena.rdfconnection._
    """,
    run / fork := true,
    run / javaOptions += "-ea",
    addCommandAlias(
        "runAll",
        ";runMain SchedulingMeetings"
    )
  )
