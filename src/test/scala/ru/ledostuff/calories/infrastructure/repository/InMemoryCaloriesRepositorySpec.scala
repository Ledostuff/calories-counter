package ru.ledostuff.calories.infrastructure.repository

import cats.effect.IO
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import cats.syntax.option._

class InMemoryCaloriesRepositorySpec extends AnyFunSuite with Matchers  {

  private val productTranslationStorage = Map(
    "Pap" -> 3490L
  )

  private val inMemoryRepository = new ProductCaloriesRepositoryInMemoryInterpreter[IO](productTranslationStorage)

  test("successfully found product calories") {
    val searchProductName = "Pap"
    val foundProduct = (for {
      foundProduct <- inMemoryRepository.getCaloriesByProductName(searchProductName)
    } yield {
      foundProduct
    }).value.unsafeRunSync()
    foundProduct shouldNot be(empty)
    foundProduct.map(_.name) shouldEqual searchProductName.some
    foundProduct.map(_.calories) shouldEqual productTranslationStorage.get(searchProductName)
  }

  test("successfully not found product calories") {
    val searchProductName = "Potatoessss"
    val foundProduct = (for {
      foundProduct <- inMemoryRepository.getCaloriesByProductName(searchProductName)
    } yield {
      foundProduct
    }).value.unsafeRunSync()
    foundProduct shouldBe(empty)
  }

}
