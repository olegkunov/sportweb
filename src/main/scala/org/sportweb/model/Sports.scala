package org.sportweb.model

import java.net.URL

import akka.http.scaladsl.model.DateTime

trait Entity

case class Sports(id: Int, name: String) extends Entity {
  override def toString = name
}

case class EventType(id: Int, name: String) extends Entity {
  override def toString = name
}

case class UserRole(id: Int, name: String) extends Entity {
  override def toString = name
}

case class User(id: Int, name: String, login: String, passwordHash: String) extends Entity { //, role: UserRole)
  override def toString = s"$name ($login)"
}

//type Person = Either[User, String]

case class Team(captain: User, members: Set[User], sports: Sports)

case class Place(owner: String, mapUrl: URL, workingHours: String, sports: Set[Sports])

case class Event(eventType: EventType, place: Place, teams: Set[Team], date: DateTime)
