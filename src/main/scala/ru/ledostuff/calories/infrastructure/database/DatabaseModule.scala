package ru.ledostuff.calories.infrastructure.database

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import ru.ledostuff.calories.config.DatabaseConfig

import cats.syntax.functor._

object DatabaseModule {
  def apply[F[_] : Async : ContextShift](config: DatabaseConfig): Resource[F, Transactor[F]] =
    for {
      fixedThreadPool  <- ExecutionContexts.fixedThreadPool[F](config.connections.poolSize)
      cachedThreadPool <- ExecutionContexts.cachedThreadPool
      xa               <- HikariTransactor.newHikariTransactor(
        driverClassName = config.driver,
        url             = config.url,
        user            = config.user,
        pass            = config.password,
        connectEC       = fixedThreadPool,
        blocker         = Blocker.liftExecutionContext(cachedThreadPool)
      )
    } yield xa

  def migrate[F[_]: Sync](config: DatabaseConfig): F[Unit] =
    Sync[F].delay {
      Flyway.configure()
        .dataSource(config.url, config.user, config.password)
        .load()
        .migrate()
    }.void

}
