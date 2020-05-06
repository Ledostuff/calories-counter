import sbt._

object Dependencies {
  val KindProjectorVersion    = "0.11.0"
  val BetterMonadicForVersion = "0.3.1"


  val CatsVersion = "2.1.1"
  val CirceVersion           = "0.13.0"
  val CirceGenericExVersion  = "0.13.0"
  val DoobieVersion          = "0.9.0"
  val H2Version              = "1.4.200"
  val FlywayVersion          = "6.3.1"
  val PureConfigVersion      = "0.12.3"
  val SttpVersion            = "2.0.7"
  val Http4sVersion          = "0.21.3"

  val FatSecretPlatformVersion = "2.0"


  val ScalaCheckVersion      = "1.14.3"
  val ScalaTestVersion       = "3.1.1"
  val ScalaTestPlusVersion   = "3.1.1.1"


  lazy val compilerPlugins = Seq(
    compilerPlugin("org.typelevel" %% "kind-projector"     % KindProjectorVersion cross CrossVersion.full),
    compilerPlugin("com.olegpy"    %% "better-monadic-for" % BetterMonadicForVersion)
  )

  object circe {
    lazy val generic = "io.circe" %% "circe-generic"        % CirceVersion
    lazy val literal = "io.circe" %% "circe-literal"        % CirceVersion
    lazy val extras  = "io.circe" %% "circe-generic-extras" % CirceGenericExVersion
    lazy val parser  = "io.circe" %% "circe-parser"         % CirceVersion

    lazy val all = Seq(generic, literal, extras, parser)
  }

  object doobie {
    lazy val core   = "org.tpolecat" %% "doobie-core"      % DoobieVersion
    lazy val h2     = "org.tpolecat" %% "doobie-h2"        % DoobieVersion
    lazy val test   = "org.tpolecat" %% "doobie-scalatest" % DoobieVersion
    lazy val hikari = "org.tpolecat" %% "doobie-hikari"    % DoobieVersion

    lazy val all = Seq(core, h2, test, hikari)
  }

  object pureconfig {
    lazy val core       = "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion
    lazy val cats       = "com.github.pureconfig" %% "pureconfig-cats"        % PureConfigVersion
    lazy val catsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion
    lazy val enumeratum = "com.github.pureconfig" %% "pureconfig-enumeratum"  % PureConfigVersion

    lazy val all = Seq(core, cats, catsEffect, enumeratum)
  }

  object sttp {
    lazy val core        = "com.softwaremill.sttp.client" %% "core"                           % SttpVersion
    lazy val circe       = "com.softwaremill.sttp.client" %% "circe"                          % SttpVersion
    lazy val catsBackend = "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % SttpVersion

    lazy val all = Seq(core, circe, catsBackend)
  }

  object http4s {
    lazy val blaze = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
    lazy val circe = "org.http4s" %% "http4s-circe"        % Http4sVersion
    lazy val dsl   = "org.http4s" %% "http4s-dsl"          % Http4sVersion

    lazy val all = Seq(blaze, circe, dsl)
  }


  lazy val catsCore = "org.typelevel" %% "cats-core"   % CatsVersion
  lazy val h2db     = "com.h2database" % "h2"          % H2Version
  lazy val flywaydb = "org.flywaydb"   % "flyway-core" % FlywayVersion

  lazy val fatsecretClient = "com.fatsecret4j" % "fatsecret-platform" % FatSecretPlatformVersion

  lazy val scalacheck     = "org.scalacheck"    %% "scalacheck"      % ScalaCheckVersion    % Test
  lazy val scalatest      = "org.scalatest"     %% "scalatest"       % ScalaTestVersion     % Test
  lazy val scalatestplus  = "org.scalatestplus" %% "scalacheck-1-14" % ScalaTestPlusVersion % Test


  lazy val all = compilerPlugins ++
    circe.all ++
    doobie.all ++
    pureconfig.all ++
    sttp.all ++
    http4s.all ++
    Seq(catsCore, scalacheck, scalatest, scalatestplus, flywaydb, h2db, fatsecretClient)

}
