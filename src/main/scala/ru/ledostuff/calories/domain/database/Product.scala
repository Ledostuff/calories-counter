package ru.ledostuff.calories.domain.database

import java.time.{Clock, Instant}

case class Product(rusName: String,
                   engNames: List[String],
                   lastUpdate: Instant = Instant.now(Clock.systemDefaultZone),
                   id: Option[Long] = None)
