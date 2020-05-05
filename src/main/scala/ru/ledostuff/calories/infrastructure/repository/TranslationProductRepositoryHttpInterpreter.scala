package ru.ledostuff.calories.infrastructure.repository

import cats.Monad
import cats.data.OptionT
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._

import io.circe._
import io.circe.generic.semiauto._

import sttp.client._
import sttp.client.circe._
import sttp.model._

import ru.ledostuff.calories.config.TranslateApiConfig
import ru.ledostuff.calories.domain.translate.{TranslatedProduct, TranslationProductRepository}
import TranslatedProductRequestParameters._
import TranslationProductRepositoryHttpInterpreter._

class I18nTranslationProductRepositoryHttpInterpreter[F[_]: Monad](translateApiConfig: TranslateApiConfig) (
  implicit sttpBackend: SttpBackend[F, Nothing, NothingT]
) extends TranslationProductRepository[F] {
  private val i18nTranslationEndpoint = "https://i18ns.com/api/v1/search"

  private def error(message: => String): F[Unit] = {
    println(message).pure
  }
  private def info(message: => String): F[Unit] = {
    println(message).pure
  }
  private def debug(message: => String): F[Unit] = {
    println(message).pure
  }

  override def translateProductName(productName: String): OptionT[F, TranslatedProduct] = {
    OptionT(
      basicRequest.header(Header.accept(MediaType.ApplicationJson.toString()))
        .header(Header.contentType(MediaType.ApplicationJson))
        .header(AuthenticationHeader, translateApiConfig.secret)
        .readTimeout(translateApiConfig.readTimeout)
        .post(uri"$i18nTranslationEndpoint")
        .body(TranslatedProductRequest(DefaultSourceTranslatedLanguage, productName))
        .response(asJson[List[TranslatedProductResponse]])
        .send()
        .flatMap(response =>
          response.body.fold({
            case HttpError(body) =>
              error(s"HttpError($body)").as(None)
            case DeserializationError(body, err) =>
              error(s"DeserializationError($body, $err)").as(None)

          },
          {
            case Nil =>
              debug(s"Translation for product $productName not found.").as(none)
            case translations =>
              val translationVariants = for {
                tr <- translations
                eachName <- tr.translations.get(DefaultTargetTranslatedLanguage).toList.flatten
              } yield  eachName
              val translatedProduct = TranslatedProduct(productName, translationVariants.toSet)
              debug(s"found translation of product $productName").as(translatedProduct.some)
          })
        )
    )
  }

}

object TranslationProductRepositoryHttpInterpreter {

  final case class TranslatedProductRequest(language: String, content: String)
  final case class TranslatedProductResponse(format: Int, translations: Map[String, List[String]])

  implicit val translatedProductRequestDecoder: Decoder[TranslatedProductRequest] = deriveDecoder
  implicit val translatedProductRequestEncoder: Encoder[TranslatedProductRequest] = deriveEncoder
  implicit val translatedProductResponseDecoder: Decoder[TranslatedProductResponse] = deriveDecoder

}

object TranslatedProductRequestParameters {
  val DefaultSourceTranslatedLanguage = "ru"
  val DefaultTargetTranslatedLanguage = "en"
  val AuthenticationHeader = "x-access-token"
}
