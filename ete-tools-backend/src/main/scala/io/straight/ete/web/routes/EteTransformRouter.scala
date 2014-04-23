package io.straight.ete.web.routes

import spray.routing._
import scala.concurrent.Future
import akka.actor.{ActorRef, ActorSelection}
import akka.pattern._
import akka.util.Timeout
import io.straight.ete.web.services.{EteOutputType, ExtractXlsRequest}
import java.io.File
import java.util.UUID

/**
 * @author rbuckland
 */
trait EteTransformRouter extends CommonHttpService  {
  val eteExtractorActor: ActorRef
  val apiEteTransform: Route = pathPrefix("ete" / "extract" / Segment ) { formatType =>
    import spray.httpx.encoding.{ NoEncoding, Gzip }
    post {
      post {
        headerValueByName("Upload-File-Name") { filename =>
          headerValueByName("Upload-File-Size") { filesize =>
            formFields('file.as[Array[Byte]]) { fileBytes:Array[Byte] =>
              complete {
                formatType.toLowerCase match {
                  case "xml" => eteExtractorActor.ask(ExtractXlsRequest(fileBytes,"unknown",EteOutputType.XML)).asInstanceOf[Future[String]]
                  case "json" => eteExtractorActor.ask(ExtractXlsRequest(fileBytes,"unknown",EteOutputType.JSON)).asInstanceOf[Future[String]]
                }
              }
            }
          }
        }
      }
    }
  }
}
