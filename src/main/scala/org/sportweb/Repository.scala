package org.sportweb

import slick.jdbc.H2Profile.api._

trait Repository {

  def db: slick.jdbc.JdbcBackend.Database

}

object InMemoryRepository extends Repository {

  val db = Database.forConfig("h2mem1")

  try {

  }
  finally db.close()

}

object PostgreRepository extends Repository {

  override def db = ???

}