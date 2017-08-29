package org.sportweb

import java.awt.Desktop
import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import org.sportweb.model.{Sports, User}
import spray.json._

import scala.io.StdIn
import scala.util.matching.Regex

object SportWebHttpServer {

  import DefaultJsonProtocol._
  import Directives._
  import SprayJsonSupport._

  implicit val sportsItemFormat = jsonFormat1(Sports)
  implicit val usersItemFormat = jsonFormat3(User)

  import SportWeb.system.dispatcher

  def routes: Route =
    get {
      path("about") {
        getFromResource("about.html")
      } ~
      path("") {
        getFromResource("index.html")
      } ~
      path("sports") {
        completeWithCollection('sports)
      } ~
      path("users") {
        completeWithCollection('users)
      } ~
      path("collection") {
        getFromResource("collection.html")
      } ~
      path(new Regex(""".*\.js""")) { s =>
        getFromResource(s)
      }

    }

  private def completeWithCollection(symbol: Symbol) = {
    symbol match {
      case 'sports =>
        val result = InMemoryRepository.getAll(symbol).mapTo[Seq[Sports]].map { data =>
          JsObject( "title" -> JsString("Виды спорта"), "data" -> JsArray(data.map(e => JsObject("name" -> JsString(e.name))).toVector)  )
        }
        complete(result)
      case 'users =>
        val result = InMemoryRepository.getAll(symbol).mapTo[Seq[User]].map { data =>
          JsObject( "title" -> JsString("Пользователи"), "data" -> JsArray(data.map(e => JsObject("name" -> JsString(e.name), "username" -> JsString(e.login))).toVector)  )
        }
        complete(result)
    }
  }

}

object SportWeb extends App {

  implicit val system = ActorSystem("sportweb")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val (interface, port) = ("localhost", 10000)
  private val bindingFuture = Http().bindAndHandle(SportWebHttpServer.routes, interface, port)

  InMemoryRepository.createTestData

//  SportWebHttpServer.startServer(interface, port, ServerSettings(ConfigFactory.load))

  println(s"Server online at http://$interface:$port/\nPress RETURN to stop...")

  if (Desktop.isDesktopSupported)
    Desktop.getDesktop.browse(new URI(s"http://$interface:$port"))

  StdIn.readLine() // let it run until user presses return

  InMemoryRepository.shutdown()

  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
