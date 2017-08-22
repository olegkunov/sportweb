package org.sportweb

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object SportWeb extends App {

  implicit val system = ActorSystem("sportweb")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val route =
    get {
      path("about") {
        getFromResource("about.html")
      } ~
      path("") {
        getFromResource("index.html")
      }
    }

  val (interface, port) = ("localhost", 10000)
  val bindingFuture = Http().bindAndHandle(route, interface, port)

  println(s"Server online at http://$interface:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return

  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
