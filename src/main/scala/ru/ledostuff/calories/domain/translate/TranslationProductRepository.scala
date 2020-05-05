package ru.ledostuff.calories.domain.translate

import cats.data.OptionT

trait TranslationProductRepository[F[_]] {

  def translateProductName(productName: String): OptionT[F, TranslatedProduct]

}
