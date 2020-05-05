package ru.ledostuff.calories.domain

import cats.effect.IO
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import ru.ledostuff.calories.infrastructure.database.InMemoryProductInterpreter
import org.scalatest.funsuite.AnyFunSuiteLike
import ru.ledostuff.calories.domain.database.{Product, ProductService}

class ProductServiceSpec extends AnyFunSuiteLike
  with Matchers
  with ScalaCheckPropertyChecks
  with DomainArbitraries {

  val productRepositoryAlgebra = new InMemoryProductInterpreter[IO]
  val productService = new ProductService[IO](productRepositoryAlgebra)

  test("save product to repository") {
    implicit val engNamesCount: Int = 3
    forAll { product: Product =>
      (for {
        productSaved <- productService.createProduct(product)
      } yield productSaved.engNames shouldEqual product.engNames).unsafeRunSync()
    }
  }

}
