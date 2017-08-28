package org.sportweb

import org.sportweb.model.{Entity, Sports, User}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.{H2Profile, PostgresProfile}
//import slick.jdbc.H2Profile.api._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure

class SportsTable(tag: Tag) extends Table[Sports](tag, "SPORTS") {
  def id = column[Int]("ID", O.AutoInc)
  def name = column[String]("NAME")
  override def * = name <> ( Sports, Sports.unapply )
}

class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("ID", O.AutoInc)
  def name = column[String]("NAME")
  def login = column[String]("LOGIN")
  def passwordHash = column[String]("PWD")
  //def role = column[UserRole]("ROLE")
  override def * = (name, login, passwordHash) <> (User.tupled, User.unapply)
}

trait Repository {

  val db: DB

  trait DB {
    def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R]
    def close(): Unit
  }

  class H2DB(und_db: slick.jdbc.H2Profile.backend.DatabaseDef) extends DB {
    override def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] = und_db.run(action)
    override def close(): Unit = und_db.close()
  }

  class PostgresDB(und_db: slick.jdbc.PostgresProfile.backend.DatabaseDef) extends DB {
    override def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] = und_db.run(action)
    override def close(): Unit = und_db.close()
  }

  private val sports = TableQuery[SportsTable]

  private val users = TableQuery[UsersTable]

  def getAll(colSymbol: Symbol): Future[Seq[Entity]] = {
    colSymbol match {
      case 'sports => db.run((for (s <- sports) yield s).result)
      case 'users =>  db.run((for (s <- users) yield s).result)
    }
  }

  def createTestData: Future[Unit] = {
    db.run(DBIO.seq(
      sports.schema.create,
      users.schema.create,
      sports ++= Seq(Sports("Football"), Sports("Hockey"), Sports("Tennis")),
      users ++= Seq(
        User("Ilya Potanin", "vanger", "abcdef"),
        User("Andrew Miroshnichenko", "mirosh", "abcdef"),
        User("Magomed Khizriyev", "megamaga", "abcdef"),
        User("Oleg Kunov", "ovk", "abcdef")
      )
    )) andThen {
      case Failure(ex) => ex.printStackTrace()
      case _ => println("Test data created")
    }
  }

  def shutdown() { db.close() }

}

object InMemoryRepository extends Repository {

  override val db = new H2DB(H2Profile.api.Database.forConfig("h2mem1"))

}

object PostgresRepository extends Repository {

  override val db = new PostgresDB(PostgresProfile.api.Database.forConfig("postgres_test"))

}