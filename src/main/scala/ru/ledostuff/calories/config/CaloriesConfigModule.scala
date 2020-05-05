package ru.ledostuff.calories.config

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.generic.auto._
import pureconfig.module.cats._
import pureconfig.module.catseffect.syntax._
import pureconfig.ConfigSource

object CaloriesConfigModule {

  def apply[F[_]](blocker: Blocker)(implicit sync: Sync[F], cs: ContextShift[F]): F[CaloriesConfig] =
    ConfigSource.defaultReference.withFallback(ConfigSource.systemProperties).loadF[F, CaloriesConfig](blocker)

}
