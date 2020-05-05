package ru.ledostuff.calories.infrastructure.repository

import cats.Functor
import cats.data.OptionT
import cats.effect.Sync
import cats.syntax.functor._
import com.fatsecret.platform.model.CompactFood
import com.fatsecret.platform.services.{FatsecretService, Response}
import ru.ledostuff.calories.config.CaloriesApiConfig
import ru.ledostuff.calories.domain.calories.{ProductCalories, ProductCaloriesRepository}

import scala.jdk.CollectionConverters._

class ProductCaloriesRepositoryHttpInterpreter[F[_]: Functor : Sync](caloriesApiConfig: CaloriesApiConfig) extends ProductCaloriesRepository[F] {

  private val foodService = new FatsecretService(caloriesApiConfig.consumerKey, caloriesApiConfig.secretKey)

  override def getCaloriesByProductName(name: String): OptionT[F, ProductCalories] = {
    val getFoodInfoFunction: Response[CompactFood] => Option[ProductCalories] = { response: Response[CompactFood] =>
      (for {
        eachFood <- response.getResults.asScala.find(_.getName.trim == name)
        serving <- foodService.getFood(eachFood.getId).getServings.asScala.headOption
      } yield {
        ProductCalories(eachFood.getName, serving.getCalories)
      })
    }
    OptionT(for {
      productCalories <- Sync[F].delay(foodService.searchFoods(name)).fmap(getFoodInfoFunction)
    } yield {
      productCalories
    })
  }
}
