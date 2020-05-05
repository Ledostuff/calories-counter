package ru.ledostuff.calories.infrastructure.repository

import cats.Applicative
import cats.data.OptionT
import ru.ledostuff.calories.domain.calories.{ProductCalories, ProductCaloriesRepository}

class ProductCaloriesRepositoryInMemoryInterpreter[F[_]: Applicative](storage: Map[String, Long]) extends ProductCaloriesRepository[F] {

  override def getCaloriesByProductName(name: String): OptionT[F, ProductCalories] = {
    OptionT.fromOption(storage.get(name).map(calories => ProductCalories(name, calories)))
  }

}
