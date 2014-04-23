package io.straight.ete.web.routes

import akka.actor._
import spray.routing.{Route, HttpService}
import spray.http.MediaTypes._
import java.io.{StringWriter, File, FileOutputStream}
import java.util.UUID
import io.straight.ete.source.extract.xls.{XlsInspector, XlsHelper}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.routing.directives.DebuggingDirectives
import io.straight.ete.web.messages.AddNewFile
import io.straight.ete.web.model.{StoredFiles, StoredFile}
import io.straight.fw.model.{UuidBaseDomain, BaseDomain, DomainValidation}
import io.straight.fw.marshalling.DomainValidationMarshaller._
import io.straight.fw.marshalling.JacksonMarshaller._
import io.straight.ete.web.services.FileService
import spray.httpx.marshalling.Marshaller
import io.straight.ete.web.messages.AddNewFile
import io.straight.ete.web.model.StoredFiles
import spray.http.HttpEntity
import scala.reflect.ClassTag
import io.straight.ete.web.messages.AddNewFile
import io.straight.ete.web.model.StoredFiles

//
// this trait defines our service behavior independently from the service actor
//
trait FileRouter extends CommonHttpService {

  val fileProcessor: ActorSelection
  val fileService: FileService

  val apiFilesRoute = pathPrefix("ete" / "uploadfile") {
//    import spray.httpx.encoding.{ NoEncoding, Gzip }
    get {
      complete {
        StoredFiles(fileService.getAll.toList)
      }
    } ~
    post {
      headerValueByName("Upload-File-Name") { filename => headerValueByName("Upload-File-Size") { filesize =>
        formFields('file.as[Array[Byte]]) { contents:Array[Byte] =>
          val f = new File("target",UUID.randomUUID().toString + ".xls")
          writeFile(f,contents)
          complete {
            fileProcessor.ask(
              AddNewFile(
                    displayName = filename,
                    filename = f.getAbsoluteFile().toString,
                    filesize = filesize.toInt)
            ).mapTo[DomainValidation[StoredFile]]
          }
        }
      }  }
    }

  }



}