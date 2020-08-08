import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossPlugin.autoImport.CrossType

ThisBuild / baseVersion := "0.0"

ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"

ThisBuild / publishGithubUser := "milanvdm"
ThisBuild / publishFullName := "Milan van der Meer"

ThisBuild / crossScalaVersions := List("0.25.0", "0.26.0-RC1", "2.12.11", "2.13.3")

ThisBuild / githubWorkflowPublishTargetBranches := Seq(
  RefPredicate.Equals(Ref.Branch("main")),
  RefPredicate.StartsWith(Ref.Tag("v"))
)
ThisBuild / githubWorkflowEnv ++= Map(
  "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}",
  "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
  "PGP_SECRET" -> s"$${{ secrets.PGP_SECRET }}"
)
ThisBuild / githubWorkflowTargetTags += "v*"

ThisBuild / githubWorkflowPublishPreamble +=
  WorkflowStep.Run(
    List("echo $PGP_SECRET | base64 -d | gpg --import"),
    name = Some("Import signing key")
  )

ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("release")))

ThisBuild / homepage := Some(url("https://github.com/typelevel/munit-cats-effect"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/typelevel/munit-cats-effect"),
    "git@github.com:typelevel/munit-cats-effect.git"
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(core.jvm, core.js)
  .settings(noPublishSettings)

val commonSettings = Seq(
  homepage := Some(url("https://github.com/typelevel/munit-cats-effect")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  libraryDependencies ++= {
    if (isDotty.value) Nil
    else Seq(scalafixSemanticdb)
  },
  scalafmtOnCompile := true
)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .settings(commonSettings)
  .settings(
    name := "munit-cats-effect",
    libraryDependencies ++= List(
      "org.typelevel" %%% "cats-effect" % "2.2.0-RC1",
      "org.scalameta" %%% "munit" % "0.7.10"
    )
  )
  .settings(dottyLibrarySettings)
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))

addCommandAlias("fmt", """scalafixAll;scalafmtSbt;scalafmtAll""")
addCommandAlias("fmtCheck", """scalafixAll --check;scalafmtSbtCheck;scalafmtCheckAll""")
