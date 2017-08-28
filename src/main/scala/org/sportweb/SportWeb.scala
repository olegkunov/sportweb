package org.sportweb

import java.awt.Desktop
import java.net.URI

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import org.sportweb.model.{Sports, User}
import spray.json.DefaultJsonProtocol

import scala.io.StdIn

object SportWebHttpServer extends Directives with SprayJsonSupport with DefaultJsonProtocol { // extends HttpApp

  implicit val sportsItemFormat = jsonFormat1(Sports)
  implicit val usersItemFormat = jsonFormat3(User)

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
      }
    }

  private def completeWithCollection(symbol: Symbol) = {
    symbol match {
      case 'sports =>
        complete(InMemoryRepository.getAll(symbol).mapTo[Seq[Sports]])
      case 'users =>
        complete(InMemoryRepository.getAll(symbol).mapTo[Seq[User]])
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
