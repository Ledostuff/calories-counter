package ru.ledostuff.calories.infrastructure

import cats.effect.IO
import ru.ledostuff.calories.config.CaloriesConfig

import scala.concurrent.ExecutionContext

package object repository {

  import pureconfig._
  import pureconfig.generic.auto._
  val caloriesAppConfig = ConfigSource.defaultReference.withFallback(ConfigSource.systemProperties).loadOrThrow[CaloriesConfig]


  lazy val testEc = ExecutionContext.Implicits.global

  implicit lazy val testCs = IO.contextShift(testEc)
}
