package org.sportweb.model

import java.net.URL

import akka.http.scaladsl.model.DateTime

trait Collection

case class Sports(name: String) extends Collection {
  override def toString = name
}

case class EventType(name: String) extends Collection {
  override def toString = name
}

case class UserRole(name: String) extends Collection {
  override def toString = name
}

case class User(name: String, login: String, passwordHash: String) extends Collection { //, role: UserRole)
  override def toString = s"$name ($login)"
}

//type Person = Either[User, String]

case class Team(captain: User, members: Set[User], sports: Sports)

case class Place(owner: String, mapUrl: URL, workingHours: String, sports: Set[Sports])

case class Event(eventType: EventType, place: Place, teams: Set[Team], date: DateTime)
