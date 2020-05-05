package ru.ledostuff.calories.infrastructure

import cats.syntax.functor._
import cats.effect.{Async, ContextShift, Effect, IO}
import doobie.util.transactor.Transactor
import ru.ledostuff.calories.config.{CaloriesConfig, DatabaseConfig}

import scala.concurrent.ExecutionContext

package object database {
  def getTransactor[F[_]: Async: ContextShift](cfg: DatabaseConfig): Transactor[F] =
    Transactor.fromDriverManager[F](
      cfg.driver, // driver classname
      cfg.url, // connect URL (driver-specific)
      cfg.user, // user
      cfg.password, // password
    )
  import pureconfig._
  import pureconfig.generic.auto._
  val caloriesAppConfig = ConfigSource.defaultReference.loadOrThrow[CaloriesConfig]


  /*
   * Provide a transactor for testing once schema has been migrated.
   */
  def initializedTransactor[F[_]: Effect: Async: ContextShift]: F[Transactor[F]] =
    for {
      _ <- DatabaseModule.migrate(caloriesAppConfig.db)
    } yield getTransactor(caloriesAppConfig.db)

  lazy val testEc = ExecutionContext.Implicits.global

  implicit lazy val testCs = IO.contextShift(testEc)

  lazy val testTransactor = initializedTransactor[IO].unsafeRunSync()

}
