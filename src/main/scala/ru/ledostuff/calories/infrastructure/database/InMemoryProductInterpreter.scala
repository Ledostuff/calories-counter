package ru.ledostuff.calories.infrastructure.database

import cats.Applicative
import ru.ledostuff.calories.domain

import scala.collection.concurrent.TrieMap
import scala.util.Random
import cats.syntax.option._
import cats.instances.option._
import cats.syntax.functor._
import cats.syntax.applicative._
import cats.syntax.traverse._
import ru.ledostuff.calories.domain.database.{Product, ProductRepositoryAlgebra}

class InMemoryProductInterpreter[F[_]: Applicative] extends ProductRepositoryAlgebra[F]{

  private val storage = new TrieMap[Long, Product]

  private val randomIds = new Random

  override def create(product: Product): F[Product] = {
    for {
      foundPet <- findByRusName(product.rusName)
    } yield {
      foundPet.getOrElse{
        val id = randomIds.nextLong()
        val savingProduct = product.copy(id = id.some)
        storage.put(id, savingProduct)
        savingProduct
      }
    }
  }

  override def update(product: Product): F[Option[Product]] = {
    product.id.traverse { id: Long =>
      storage.update(id, product)
      product.pure[F]
    }
  }

  override def get(productId: Long): F[Option[Product]] = {
    storage.get(productId).pure[F]
  }

  override def delete(productId: Long): F[Option[Product]] = {
    storage.remove(productId).pure[F]
  }

  override def findByRusName(name: String): F[Option[Product]] = {
    storage.values.find(p => p.rusName == name).pure[F]
  }
}
