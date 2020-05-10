package ru.ledostuff.calories.infrastructure.services

import java.time.{Clock, Instant}

import cats.data.{EitherT, OptionT}
import cats.effect.Sync
import cats.implicits._
import ru.ledostuff.calories.domain.calories.{ProductCalories, ProductCaloriesRepository}
import ru.ledostuff.calories.domain.database.{Product, ProductService}
import ru.ledostuff.calories.domain.translate.{TranslatedProduct, TranslationProductRepository}
import ru.ledostuff.calories.infrastructure.logging.Log
import ru.ledostuff.calories.infrastructure.services.ProductCaloriesService.{ErrorResponse, ProductCaloriesInfo, ProductCaloriesNotFound, ProductCaloriesResponse, SuccessResponse, TranslationNotFound}

class ProductCaloriesService[F[_]: Sync](productService: ProductService[F],
                                         translateService: TranslationProductRepository[F],
                                         productCaloriesService: ProductCaloriesRepository[F],
                                         log: Log[F]) {

  def getProductCalories(name: String): F[ProductCaloriesResponse] = {
    // Found existence product English names
    val existsProductCalories = for {
      foundProductInfo <- productService.findByName(name)
      caloriesInfo     <- {
        val engNamesRequests = foundProductInfo.engNames.toList.map { engName =>
          OptionT.liftF(log.info(s"search calories for existence name $engName")) *>
          productCaloriesService.getCaloriesByProductName(engName)
        }
        engNamesRequests.sequence
      }
      time <- OptionT.liftF(Sync[F].delay(Instant.now(Clock.systemDefaultZone)))
      _ <- {
        productService.update(foundProductInfo.copy(engNames = caloriesInfo.map(_.name),
          lastUpdate = time))
      }
    } yield {
      caloriesInfo.headOption
    }
    // If names did not found, go to translation service, save returned product English names and after that use calories service
    val result = OptionT(existsProductCalories.value.map(_.flatten)).map(
      { existsCalories: ProductCalories =>
        ProductCaloriesInfo(name, existsCalories.calories)
      }
    ).getOrElseF({
      val resultOrError: EitherT[F, ErrorResponse, ProductCaloriesInfo] = for {

        _                     <- EitherT.liftF(log.info(s"search translation for name $name"))
        translatedProductInfo <- EitherT.fromOptionF[F, ErrorResponse, TranslatedProduct]({
          translateService.translateProductName(name).value
        }, TranslationNotFound)

        savedProduct          <- EitherT.liftF[F, ErrorResponse, Product](productService.createProduct(Product(name, translatedProductInfo.translatedNames.toList)))

        caloriesInfo          <- {
          val engNamesRequests = savedProduct.engNames.map { engName =>
            (EitherT.liftF[F, ErrorResponse, Unit](log.info(s"search calories for name $engName")) *>
              EitherT.fromOptionF[F, ErrorResponse, ProductCalories](productCaloriesService.getCaloriesByProductName(engName).value, ProductCaloriesNotFound)).value
          }
          EitherT.liftF(engNamesRequests.sequence.map(l => l.collect{case Right(e) => e})).ensure(ProductCaloriesNotFound)(_.nonEmpty)
        }
        time                  <- EitherT.liftF(Sync[F].delay(Instant.now(Clock.systemDefaultZone)))

        _                     <- EitherT.liftF(log.info(s"save updated calories names ${caloriesInfo.mkString(",")}"))
        _                     <- {
          EitherT.fromOptionF[F, ErrorResponse, Product](productService.update(savedProduct.copy(engNames = caloriesInfo.map(_.name),
            lastUpdate = time)).value, ProductCaloriesNotFound)
        }
      } yield {
        ProductCaloriesInfo(name, caloriesInfo.head.calories)
      }
      val caloriesSearchResult: F[ProductCaloriesResponse] = resultOrError.value.map {
        case Right(success) => success
        case Left(error) => error
      }
      caloriesSearchResult
    })
    result
  }

}

object ProductCaloriesService {

  sealed trait ProductCaloriesResponse

  sealed trait ErrorResponse extends ProductCaloriesResponse

  sealed trait SuccessResponse extends ProductCaloriesResponse

  case object TranslationNotFound extends ErrorResponse

  case object ProductCaloriesNotFound extends ErrorResponse

  final case class ProductCaloriesInfo(name: String, calories: BigDecimal) extends SuccessResponse

}
