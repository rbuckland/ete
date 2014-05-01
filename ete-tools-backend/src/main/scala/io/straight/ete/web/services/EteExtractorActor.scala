package io.straight.ete.web.services

import akka.actor.Actor
import java.io._
import io.straight.ete.source.extract.xls.XlsInspector
import io.straight.ete.source.extract.EteExecutor
import java.util.UUID
import io.straight.ete.web.services.EteOutputType.OutputType

/**
 * @author rbuckland
 */
class EteExtractorActor extends Actor {
  def receive = {
    case extract: ExtractXlsRequest => sender ! convertFileToXML(extract)
  }


  def convertFileToXML(extract: ExtractXlsRequest) : String = {

    val f = saveByteArrayToFile(extract.data)

    val eteConfig = XlsInspector.createConfig(f,extract.name)
    val outputStream = new ByteArrayOutputStream()
    extract.outputType match {
        // TODO create some nice formatter enums inside ete
      case EteOutputType.XML => EteExecutor.generate(eteConfig,outputStream,Vector.empty,"xml", false)
      case EteOutputType.JSON => EteExecutor.generate(eteConfig,outputStream,Vector.empty,"json", false)

    }

    return new String(outputStream.toByteArray(),"UTF-8")
  }

  def saveByteArrayToFile(data: Array[Byte]) : File = {
    val f = new File("target",UUID.randomUUID().toString)
    println("Wrote out >>> " + f.getAbsoluteFile)
    val fos: FileOutputStream = new FileOutputStream(f)
    try {
      fos.write(data)
    } finally {
      fos.close()
    }
    return f
  }
}

case class ExtractXlsRequest(data: Array[Byte], name:String, outputType: OutputType)

object EteOutputType extends Enumeration {
  type OutputType = Value
  val XML, JSON = Value
}
