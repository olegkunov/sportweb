package org.sportweb

import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

trait Repository {



}

object InMemoryRepository extends Repository {

  val db = Database.forConfig("h2mem1")

  try {

  }
  finally db.close()

}

object PostgreRepository extends Repository {



}