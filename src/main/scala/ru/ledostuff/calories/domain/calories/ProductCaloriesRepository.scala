package ru.ledostuff.calories.domain.calories

import cats.data.OptionT

trait ProductCaloriesRepository[F[_]] {

  def getCaloriesByProductName(name: String): OptionT[F, ProductCalories]

}
