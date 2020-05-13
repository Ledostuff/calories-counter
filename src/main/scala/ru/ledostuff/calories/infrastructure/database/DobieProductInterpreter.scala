package ru.ledostuff.calories.infrastructure.database

import cats.data.OptionT
import cats.syntax.functor._
import cats.syntax.option._
import doobie.implicits.legacy.instant._
import cats.effect.Bracket
import doobie._
import doobie.util.update.Update0
import ru.ledostuff.calories.domain
import doobie.implicits._
import ru.ledostuff.calories.domain.database.{Product, ProductRepositoryAlgebra}

private object ProductSQL {

  /* This is used to marshal our sets of strings */
  implicit val SetStringMeta: Meta[List[String]] =
    Meta[String].imap(_.split(',').toList)(_.mkString(","))

  def insert(product: Product): Update0 = {
    sql"""
         INSERT INTO PRODUCT(RUS_NAME, ENG_NAMES, LAST_UPDATE)
         VALUES (${product.rusName}, ${product.engNames}, ${product.lastUpdate})
       """.update
  }

  def update(product: Product, id: Long): Update0 = {
    sql"""
         UPDATE PRODUCT
         SET RUS_NAME = ${product.rusName}, LAST_UPDATE = ${product.lastUpdate}, ENG_NAMES = ${product.engNames}
         WHERE ID = $id
       """.update
  }

  def select(id: Long): Query0[Product] = {
    sql"""
         SELECT RUS_NAME, ENG_NAMES, LAST_UPDATE, ID
         FROM PRODUCT
         WHERE ID = $id
       """.query[Product]
  }

  def delete(id: Long): Update0 = sql"""
    DELETE FROM PRODUCT WHERE ID = $id
  """.update

  def selectByRusName(rusName: String): Query0[Product] = sql"""
    SELECT RUS_NAME, ENG_NAMES, LAST_UPDATE, ID
    FROM PRODUCT
    WHERE RUS_NAME = $rusName
  """.query[Product]

}

class DobieProductInterpreter[F[_]: Bracket[*[_], Throwable]](val xa: Transactor[F])
  extends ProductRepositoryAlgebra[F] {
  import ProductSQL._

  override def create(product: Product): F[Product] = {
    OptionT(findByRusName(product.rusName)).getOrElseF {
      insert(product)
        .withUniqueGeneratedKeys[Long]("ID")
        .map(id => product.copy(id = id.some))
        .transact(xa)
    }
  }

  override def update(product: Product): F[Option[Product]] = {
    OptionT
      .fromOption[ConnectionIO](product.id)
      .semiflatMap(id => ProductSQL.update(product, id).run.as(product))
      .transact(xa).value
  }

  override def get(productId: Long): F[Option[Product]] = {
    select(productId)
      .option
      .transact(xa)
  }

  override def delete(productId: Long): F[Option[Product]] = {
    OptionT(get(productId))
        .semiflatMap(product =>
          ProductSQL.delete(productId)
            .run
            .transact(xa).as(product)
        )
      .value
  }

  override def findByRusName(name: String): F[Option[Product]] = {
    selectByRusName(name)
      .option
      .transact(xa)
  }
}
