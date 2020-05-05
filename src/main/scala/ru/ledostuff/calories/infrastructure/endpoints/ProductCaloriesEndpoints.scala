package ru.ledostuff.calories.infrastructure.endpoints

import cats.effect.Sync
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import org.http4s.{EntityDecoder, HttpRoutes, Request, Response}
import ru.ledostuff.calories.infrastructure.services.ProductCaloriesService
import cats.syntax.flatMap._
import ru.ledostuff.calories.infrastructure.services.ProductCaloriesService.{ProductCaloriesInfo, ProductCaloriesNotFound, TranslationNotFound}
import io.circe.syntax._
import io.circe.generic.auto._

class ProductCaloriesEndpoints[F[_]: Sync] extends Http4sDsl[F] {

  implicit val productCaloriesInfoDecoder: EntityDecoder[F, ProductCaloriesInfo] = jsonOf[F, ProductCaloriesInfo]

  object ProductNameQueryParamMatcher extends QueryParamDecoderMatcher[String]("name")
  private def getCaloriesByName(productService: ProductCaloriesService[F]): PartialFunction[Request[F], F[Response[F]]] = {
    case GET -> Root :? ProductNameQueryParamMatcher(name) =>
      productService.getProductCalories(name).flatMap {
        case caloriesInfo: ProductCaloriesInfo => Ok(caloriesInfo.asJson)
        case TranslationNotFound => NotFound("Product translation from Russian to English was not found")
        case ProductCaloriesNotFound => NotFound("Product calories information was not found")
      }
  }


  def endpoints(productService: ProductCaloriesService[F]): HttpRoutes[F] = {
    val endpoints = getCaloriesByName(productService)
    HttpRoutes.of[F](endpoints)
  }

}

object ProductCaloriesEndpoints {
  def endpoints[F[_]: Sync](productService: ProductCaloriesService[F]): HttpRoutes[F] =
    new ProductCaloriesEndpoints[F].endpoints(productService)
}

