package ru.ledostuff.calories.infrastructure.repository

import cats.data.OptionT
import cats.effect.Sync
import com.fatsecret.platform.model.CompactFood
import com.fatsecret.platform.services.{FatsecretService, Response}
import ru.ledostuff.calories.config.CaloriesApiConfig
import ru.ledostuff.calories.domain.calories.{ProductCalories, ProductCaloriesRepository}

import scala.jdk.CollectionConverters._

class ProductCaloriesRepositoryHttpInterpreter[F[_]: Sync](caloriesApiConfig: CaloriesApiConfig) extends ProductCaloriesRepository[F] {

  private val foodService = new FatsecretService(caloriesApiConfig.consumerKey, caloriesApiConfig.secretKey)

  override def getCaloriesByProductName(name: String): OptionT[F, ProductCalories] = {
    val getFoodInfoFunction: Response[CompactFood] => OptionT[F, ProductCalories] = { response: Response[CompactFood] =>
      for {
        eachFood <- OptionT.fromOption(response.getResults.asScala.find(_.getName.trim == name))
        serving <- OptionT(Sync[F].delay(foodService.getFood(eachFood.getId).getServings.asScala.headOption))
      } yield {
        ProductCalories(eachFood.getName, serving.getCalories)
      }
    }
    OptionT.liftF(Sync[F].delay(foodService.searchFoods(name))).flatMap(getFoodInfoFunction)
  }
}
