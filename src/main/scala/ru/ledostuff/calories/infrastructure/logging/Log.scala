package ru.ledostuff.calories.infrastructure.logging

import cats.effect.Sync

class Log[F[_] : Sync] {
  def error(message: => String): F[Unit] = {
    Sync[F].delay(println(s"ERROR - $message"))
  }
  def info(message: => String): F[Unit] = {
    Sync[F].delay(println(s"INFO - $message"))
  }
  def debug(message: => String): F[Unit] = {
    Sync[F].delay(println(s"DEBUG - $message"))
  }
}
