package ru.ledostuff.calories.config

final case class DatabaseConfig(url: String,
                                driver: String,
                                user: String,
                                password: String,
                                connections: DatabaseConnectionsConfig)

final case class DatabaseConnectionsConfig(poolSize: Int)
