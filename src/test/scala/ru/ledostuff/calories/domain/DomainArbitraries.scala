package ru.ledostuff.calories.domain

import java.time.Instant

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import ru.ledostuff.calories.domain.database.Product

trait DomainArbitraries {

  implicit val instant = Arbitrary[Instant] {
    for {
      millis <- Gen.posNum[Long]
    } yield Instant.ofEpochMilli(millis)
  }

  implicit def product(implicit numTranslations: Int): Arbitrary[Product] = Arbitrary {
    for {
      rusName <- arbitrary[String]
      engNames <- Gen.listOfN(numTranslations, Gen.alphaStr)
      lastUpdate <- instant.arbitrary
      id <- Gen.option(Gen.posNum[Long])
    } yield database.Product(rusName, engNames, lastUpdate, id)
  }

}

object DomainArbitraries extends DomainArbitraries
