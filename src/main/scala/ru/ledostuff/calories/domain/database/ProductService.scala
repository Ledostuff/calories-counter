package ru.ledostuff.calories.domain.database

import cats.Functor
import cats.data.OptionT
import cats.syntax.functor._

class ProductService[F[_]](repo: ProductRepositoryAlgebra[F]) {

  def createProduct(product: Product): F[Product] =
    repo.create(product)

  def get(id: Long): OptionT[F, Product] =
    OptionT(repo.get(id))

  def update(product: Product): OptionT[F, Product] =
    OptionT(repo.update(product))

  def delete(id: Long)(implicit F: Functor[F]): F[Unit] =
    repo.delete(id).as(())

  def findByName(name: String): OptionT[F, Product] =
    OptionT(repo.findByRusName(name))

}

object ProductService {
  def apply[F[_]](repo: ProductRepositoryAlgebra[F]): ProductService[F] = new ProductService(repo)
}
