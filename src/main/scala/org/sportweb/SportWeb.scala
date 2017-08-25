package org.sportweb

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.typesafe.config.{ConfigBeanFactory, ConfigFactory}
import org.sportweb.model.Entity

import scala.io.StdIn

object SportWebHttpServer { // extends HttpApp

  def routes =
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
  StdIn.readLine() // let it run until user presses return

  InMemoryRepository.shutdown()

  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
