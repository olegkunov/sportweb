package org.sportweb

import org.sportweb.model.{Collection, Sports, User, UserRole}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure

trait Repository { }

class SportsTable(tag: Tag) extends Table[Sports](tag, "SPORTS") {
  def id = column[Int]("ID", O.AutoInc)
  def name = column[String]("NAME")
  override def * = name <> (Sports, Sports.unapply)
}

class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("ID", O.AutoInc)
  def name = column[String]("NAME")
  def login = column[String]("LOGIN")
  def passwordHash = column[String]("PWD")
  //def role = column[UserRole]("ROLE")
  override def * = (name, login, passwordHash) <> (User.tupled, User.unapply)
}

object InMemoryRepository extends Repository {

  def getAll(colSymbol: Symbol): Future[Seq[Collection]] = {
    colSymbol match {
      case 'sports => db.run((for (s <- sports) yield s).result)
      case 'users =>  db.run((for (s <- users) yield s).result)
    }
  }

  val db = Database.forConfig("h2mem1")

  val sports = TableQuery[SportsTable]

  val users = TableQuery[UsersTable]

  def createTestData = {
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

}

object PostgreRepository extends Repository {



}