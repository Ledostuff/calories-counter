package ru.ledostuff.calories

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync, Timer}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import ru.ledostuff.calories.config.CaloriesConfigModule
import ru.ledostuff.calories.domain.database.ProductService
import ru.ledostuff.calories.infrastructure.database.{DatabaseModule, DobieProductInterpreter}
import ru.ledostuff.calories.infrastructure.endpoints.ProductCaloriesEndpoints
import ru.ledostuff.calories.infrastructure.logging.Log
import ru.ledostuff.calories.infrastructure.repository.{I18nTranslationProductRepositoryHttpInterpreter, ProductCaloriesRepositoryHttpInterpreter}
import ru.ledostuff.calories.infrastructure.services.ProductCaloriesService
import sttp.client.SttpBackend
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

object Server extends IOApp {

  def init[F[_] : Timer : ConcurrentEffect : ContextShift]() =
    for {
      config <- Blocker[F].evalMap(CaloriesConfigModule[F])

      tx <- DatabaseModule[F](config.db)
      log = new Log[F]

      implicit0(
      httpBackend: SttpBackend[F, Nothing, WebSocketHandler]
      ) <- AsyncHttpClientCatsBackend.resource[F]()
      /* use Sync[F].delay if resource or variable is initialized with side effects:
       1. Read file system
       2. Initialize some pools
       3. Some other variables
       etc.
      */
      translateRepository = new I18nTranslationProductRepositoryHttpInterpreter[F](config.translateApi, log)

      productCaloriesRepository = new ProductCaloriesRepositoryHttpInterpreter[F](config.caloriesApi)

      productRepositoryAlgebra = new DobieProductInterpreter[F](tx)
      productService = new ProductService[F](productRepositoryAlgebra)

      productCaloriesService = new ProductCaloriesService[F](productService,
        translateRepository,
        productCaloriesRepository,
        log)

      router = Router(
        "/product" -> ProductCaloriesEndpoints.endpoints[F](productCaloriesService)
      ).orNotFound

      _      <- Resource.liftF(DatabaseModule.migrate[F](config.db))
      server <- BlazeServerBuilder[F]
        .bindHttp(port = 8080)
        .withHttpApp(router)
        .resource
    } yield server

  override def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- init().use(_ => IO.never)
    } yield ExitCode.Success
}

