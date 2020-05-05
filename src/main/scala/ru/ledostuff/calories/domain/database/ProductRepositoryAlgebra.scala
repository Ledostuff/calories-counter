package ru.ledostuff.calories.domain.database

trait ProductRepositoryAlgebra[F[_]] {

  def create(product: Product): F[Product]

  def update(product: Product): F[Option[Product]]

  def get(productId: Long): F[Option[Product]]

  def delete(productId: Long): F[Option[Product]]

  def findByRusName(name: String): F[Option[Product]]

}
