package ru.ledostuff.calories.domain

import io.circe.Encoder
import io.circe.generic.semiauto._

package object calories {
  implicit val productCaloriesEncoder: Encoder[ProductCalories] = deriveEncoder
}
