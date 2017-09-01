package org.sportweb

import org.sportweb.model.{Entity, Sports, Team, User}
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.{H2Profile, PostgresProfile}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
//import slick.jdbc.H2Profile.api._
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure

class SportsTable(tag: Tag) extends Table[Sports](tag, "SPORTS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME")
  override def * = (id, name) <> ( Sports.tupled, Sports.unapply )
}

class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("ID", O.AutoInc, O.PrimaryKey)
  def name = column[String]("NAME")
  def login = column[String]("LOGIN")
  def passwordHash = column[String]("PWD")
  //def role = column[UserRole]("ROLE")
  override def * = (id, name, login, passwordHash) <> (User.tupled, User.unapply)
}

class TeamsTable(tag: Tag) extends Table[Team](tag, "TEAMS") { this: DBComponent =>
  def id = column[Int]("ID")

  def captain_id = column[Int]("CAPTAIN_ID")
  def captain = foreignKey("CAPT_FK", captain_id, Tables.users)(_.id)

  def member_id = column[Int]("MEMBER_ID")
  def member = foreignKey("MEMBER_FK", member_id, Tables.users)(_.id)

  def userIdToTeam(captainID: Int): Future[Team] = {
    val q = captain.filter(_.id === captainID).result.head
    val user = db.run(q)
    ???
  }
  override def * = ??? // captain_id <> ( userIdToTeam, (team: Team) => Some(team.captain.hashCode()) )
}

object Tables {

  val sports = TableQuery[SportsTable]

  val users = TableQuery[UsersTable]

}

trait DBComponent {

  val db: DB

  trait DB {
    def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R]
    def close(): Unit
  }

}

trait Repository extends DBComponent {

  class H2DB(und_db: slick.jdbc.H2Profile.backend.DatabaseDef) extends DB {
    override def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] = und_db.run(action)
    override def close(): Unit = und_db.close()
  }

  class PostgresDB(und_db: slick.jdbc.PostgresProfile.backend.DatabaseDef) extends DB {
    override def run[R](action: DBIOAction[R, NoStream, Nothing]): Future[R] = und_db.run(action)
    override def close(): Unit = und_db.close()
  }

  def getAll(colSymbol: Symbol): Future[Seq[Entity]] = {
    colSymbol match {
      case 'sports => db.run((for (s <- Tables.sports) yield s).result)
      case 'users =>  db.run((for (s <- Tables.users) yield s).result)
    }
  }

  def shutdown() { db.close() }

}

object InMemoryRepository extends Repository {

  override val db = new H2DB(H2Profile.api.Database.forConfig("h2mem1"))

  def createTestData: Future[Unit] = {
    db.run(DBIO.seq(
      Tables.sports.schema.create,
      Tables.users.schema.create,
      Tables.sports ++= Seq(Sports(0, "Football"), Sports(0, "Hockey"), Sports(0, "Tennis"), Sports(0, "Magic the Gathering")),
      Tables.users ++= Seq(
        User(0, "Ilya Potanin", "vanger", "abcdef"),
        User(0, "Andrew Miroshnichenko", "mirosh", "abcdef"),
        User(0, "Magomed Khizriyev", "magomed", "abcdef"),
        User(0, "Oleg Kunov", "ovk", "abcdef")
      )
    )) andThen {
      case Failure(ex) => ex.printStackTrace()
      case _ => println("Test data created")
    }
  }

}

object PostgresRepository extends Repository {

  override val db = new PostgresDB(PostgresProfile.api.Database.forConfig("postgres_test"))

}