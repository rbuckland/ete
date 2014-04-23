package io.straight.ete.web

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import io.straight.fw.service.UuidRepository
import com.escalatesoft.subcut.inject.NewBindingModule
import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._
import io.straight.ete.web.model.StoredFile
import io.straight.ete.web.routes.CoreWebRouter
import io.straight.ete.web.services.{FileService, FileProcessor}
import io.straight.fw.model.UuidGenerator

object Boot extends App {

  var port = 8080
  if (args.length == 2 && "--port".equals(args(0))) {
    port = args(1).toInt
  }

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")
  implicit val bindingModule = CoreBindingModule

  // create and start our service actor
  val service = system.actorOf(Props(new CoreWebRouter()), "ete-tools-web")

  system.actorOf(Props(new FileProcessor()), name=FileProcessor.ACTOR_NAME)


  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ! Http.Bind(service, interface = "0.0.0.0", port = port)
}

object CoreBindingModule extends NewBindingModule( module => {

  import module._   // can now use bind directly
  import BindingKeys._  // use the Binding IDs conveniently

  bind [Timeout] idBy DefaultTimeout toSingle Timeout(10 seconds)

  /*
   * HashMap repositories
   */
  bind [UuidRepository[StoredFile]]  toSingle new UuidRepository[StoredFile]()
  bind [UuidGenerator[StoredFile]]  toSingle new UuidGenerator[StoredFile](classOf[StoredFile]) {}
  bind [FileService]  toProvider { implicit module => new FileService() }

})

/**
 * @author rbuckland
 */
object BindingKeys {

  val DefaultTimeout = 'DefaultTimeout
}

