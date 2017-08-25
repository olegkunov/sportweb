package org.sportweb

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import org.sportweb.model.Entity

import scala.io.StdIn

object SportWeb extends App {

  implicit val system = ActorSystem("sportweb")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val route =
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
      }
    }

  private def completeWithCollection(symbol: Symbol) = {
    onSuccess(InMemoryRepository.getAll(symbol)) { s =>
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,
        s"${s.foldLeft[String]("")((str: String, elem: Entity) => s"$str ${elem.toString} <p>")} <p><a href='/'>back</a>"
      ))
    }
  }

  private val (interface, port) = ("localhost", 10000)
  private val bindingFuture = Http().bindAndHandle(route, interface, port)

  InMemoryRepository.createTestData

  println(s"Server online at http://$interface:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  InMemoryRepository.shutdown()

  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
