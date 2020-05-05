package ru.ledostuff.calories.infrastructure.repository

import cats.Applicative
import cats.data.OptionT
import ru.ledostuff.calories.domain.translate.{TranslatedProduct, TranslationProductRepository}

class TranslationProductRepositoryInMemoryInterpreter[F[_]: Applicative](productTranslations: Map[String, Set[String]])
  extends TranslationProductRepository[F] {

  override def translateProductName(productName: String): OptionT[F, TranslatedProduct] = {
    OptionT.fromOption(productTranslations.get(productName).map(set => TranslatedProduct(productName, set)))
  }
}
