package ru.ledostuff.calories.infrastructure.repository

import cats.effect.IO
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import cats.syntax.option._


class InMemoryTranslationRepositorySpec extends AnyFunSuite with Matchers {

  private val productTranslationStorage = Map(
    "пюре" -> Set("mashed", "Puree", "Pap", "Mashed Potatoes")
  )

  private val inMemoryRepository = new TranslationProductRepositoryInMemoryInterpreter[IO](productTranslationStorage)

  test("successfully found product translation") {
    val searchProductName = "пюре"
    val foundProduct = (for {
      foundProduct <- inMemoryRepository.translateProductName(searchProductName)
    } yield {
      foundProduct
    }).value.unsafeRunSync()
    foundProduct shouldNot be(empty)
    foundProduct.map(_.name) shouldEqual searchProductName.some
    foundProduct.map(_.translatedNames) shouldEqual productTranslationStorage.get(searchProductName)
  }

  test("successfully not found product translation") {
    val searchProductName = "картошка жареная"
    val foundProduct = (for {
      foundProduct <- inMemoryRepository.translateProductName(searchProductName)
    } yield {
      foundProduct
    }).value.unsafeRunSync()
    foundProduct shouldBe(empty)
  }

}
