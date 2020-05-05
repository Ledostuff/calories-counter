package ru.ledostuff.calories.infrastructure.database

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import ru.ledostuff.calories.domain.DomainArbitraries._

class ProductQueryTypeCheckSpec extends AnyFunSuite with Matchers with IOChecker {
  override val transactor: Transactor[IO] = testTransactor

  import ProductSQL._
  test("Typecheck product queries") {
    implicit val numEngNames: Int = 4
    product.arbitrary.sample.foreach { p =>
      check(insert(p))
      p.id.foreach(id => check(ProductSQL.update(p, id)))
    }

    check(select(1L))
    check(delete(1L))
    check(selectByRusName("макароны"))
  }

}
