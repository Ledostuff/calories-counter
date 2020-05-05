package ru.ledostuff.calories.infrastructure.repository

import cats.effect.IO
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers


class HttpProductCaloriesRepositorySpec extends AnyFunSuite with Matchers {

  private val httpProductCaloriesRepository = new ProductCaloriesRepositoryHttpInterpreter[IO](caloriesAppConfig.caloriesApi)

  test("successfully found product calories over http") {
    val searchProductName = "Mashed Potatoes"
    val foundProductCalories = (for {
      foundProductCalories <- httpProductCaloriesRepository.getCaloriesByProductName(searchProductName)
    } yield {
      foundProductCalories
    }).value.unsafeRunSync()

    foundProductCalories shouldNot be(empty)
    foundProductCalories.map(_.name) shouldNot be(empty)
    foundProductCalories.map(_.calories) shouldNot be(empty)
  }

}
