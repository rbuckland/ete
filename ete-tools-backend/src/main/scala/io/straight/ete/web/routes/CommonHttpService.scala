package io.straight.ete.web.routes

import akka.util.Timeout
import spray.routing.HttpService
import java.io.{FileOutputStream, File}

/**
 * @author rbuckland
 */
trait CommonHttpService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout: Timeout

  def writeFile(f: File, contents:Array[Byte]) = {

    val fos: FileOutputStream = new FileOutputStream(f);
    try {
      fos.write(contents);
    } finally {
      fos.close();
    }
  }
}
