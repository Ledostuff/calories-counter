package ru.ledostuff.calories.config

import scala.concurrent.duration.Duration

final case class ServerConfig(host: String, port: String)
final case class TranslateApiConfig(secret: String, readTimeout: Duration)
final case class CaloriesApiConfig(consumerKey: String, secretKey: String)
final case class CaloriesConfig(db: DatabaseConfig,
                                server: ServerConfig,
                                translateApi: TranslateApiConfig,
                                caloriesApi: CaloriesApiConfig)
