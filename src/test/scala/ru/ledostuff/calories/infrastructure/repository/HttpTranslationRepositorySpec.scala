package ru.ledostuff.calories.infrastructure.repository

import cats.effect.{IO, Resource, Sync}
import cats.syntax.option._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import sttp.client.SttpBackend
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend


class HttpTranslationRepositorySpec extends AnyFunSuite with Matchers {

  private val httpTranslationRepository = for {
    implicit0(
    httpBackend: SttpBackend[IO, Nothing, WebSocketHandler]
    ) <- AsyncHttpClientCatsBackend.resource[IO]()
    translateRepository <- Resource.liftF(Sync[IO].delay( new I18nTranslationProductRepositoryHttpInterpreter[IO](caloriesAppConfig.translateApi)))
  } yield translateRepository

  test("successfully found product translation over http") {
    val searchProductName = "пюре"
    val foundProductResource = for {
      foundProduct <- httpTranslationRepository.map(_.translateProductName(searchProductName))
    } yield {
      foundProduct
    }
    val foundProduct = foundProductResource.use{ foundProductIO =>
      foundProductIO.value
    }.unsafeRunSync()

    foundProduct shouldNot be(empty)
    foundProduct.map(_.name) shouldEqual searchProductName.some
    foundProduct.map(_.translatedNames).toSet.flatten shouldNot be(empty)
  }

}
