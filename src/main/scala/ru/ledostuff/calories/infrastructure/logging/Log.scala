package ru.ledostuff.calories.infrastructure.logging

import cats.Applicative
import cats.syntax.applicative._

class Log[F[_]: Applicative] {
  def error(message: => String): F[Unit] = {
    println(s"ERROR - $message").pure
  }
  def info(message: => String): F[Unit] = {
    println(s"INFO - $message").pure
  }
  def debug(message: => String): F[Unit] = {
    println(s"DEBUG - $message").pure
  }
}
