package io.straight.ete.web.routes

import java.io.File
import java.util.UUID
import org.apache.commons.lang3.SystemUtils
import org.parboiled.common.FileUtils
import io.straight.dbtools.xls2db.SpreadSheetConfig
import java.sql.DriverManager
import spray.http.MediaTypes._
import spray.http.HttpHeaders._
import io.straight.ete.web.util.ZipFileUtil
import spray.http.{HttpHeader, DateTime, ContentType}
import spray.httpx.marshalling.{MetaMarshallers, BasicMarshallers}
import spray.routing.RoutingSettings
import akka.actor.ActorRefFactory
import io.straight.ete.source.extract.xls.XlsHelper

/**
 * @author rbuckland
 */
trait H2DatabaseGenerator extends CommonHttpService {


  val generateH2DbRoute = pathPrefix("h2generator") {

//    headerValueByName("Upload-File-Name") { filename =>
//      headerValueByName("DatabaseName") { dbname =>
        formFields('xlsFile.as[Array[Byte]],'databaseName.as[String]) { (contents,databaseName) =>

            val sessionId = UUID.randomUUID().toString
            val workFolder = new File(SystemUtils.getJavaIoTmpDir,sessionId)

            FileUtils.forceMkdir(workFolder)

            val xlsSanitised = sanitiseFileName("unknown")
            val dbnameSanitised = sanitiseDBName(databaseName)

            val f = new File(workFolder,sessionId + "_" + xlsSanitised)
            writeFile(f,contents)

            implicit val bufferMarshaller = BasicMarshallers.byteArrayMarshaller(new ContentType(`application/zip`, None))

            val (dbZipFilename, zipFile) = generateH2Database(f,xlsSanitised,dbnameSanitised,sessionId,workFolder)

            respondWithHeaders(zipHeaders(dbZipFilename)) {
                complete(FileUtils.readAllBytes(zipFile))
            }
//        }
//      }
    }
  }

  def zipHeaders(filename: String) : List[HttpHeader] = {

    val modified = `Last-Modified`(DateTime(System.currentTimeMillis()))
    val attachment = `Content-Disposition`("attachment", Map(("filename", filename)))

    return modified :: attachment :: Nil
  }

    //Stream marshaller if we are going to serve this file in chunks
  //  implicit val streamBufferMarshaller = MetaMarshallers.streamMarshaller[Array[Byte]]
    //            if (0 < settings.FileChunkingThresholdSize && settings.FileChunkingThresholdSize <= file.length)
    //              complete(file.toByteArrayStream(settings.FileChunkingChunkSize.toInt))
    //            else complete(FileUtils.readAllBytes(file))


  def generateH2Database(xlsFile: File,
                         xlsSanitised: String,
                         dbnameSanitised: String,
                         sessionId: String,
                         workFolder: File):(String,File) = {

    import scala.collection.JavaConverters._

    val dbPopulator = new io.straight.dbtools.xls2db.POIXLSDatabasePopulator()
    val h2Directory = workFolder.getAbsolutePath + "/database"
    val h2JdbcUrl = "jdbc:h2:file:" + h2Directory + "/" + dbnameSanitised
    val workbook = XlsHelper.loadWorkbook(xlsFile);

    dbPopulator.setSpreadsheets(
      (for (
            i <- 0 until workbook.getNumberOfSheets
          ) yield new SpreadSheetConfig(workbook.getSheetName(i),xlsFile,workbook.getSheetName(i))
      ).to[List].asJava
      )

    // populate it
    Class.forName("org.h2.Driver")
    val connection = DriverManager.getConnection(h2JdbcUrl)
    dbPopulator.populate(connection)
    connection.close()

    println("new db created at: " + h2JdbcUrl)

    // Zip it up
    val dbZipFilename = dbnameSanitised + ".zip"
    val zipFile = new File(workFolder,dbnameSanitised + ".zip")
    ZipFileUtil.zipDirectory(new File(h2Directory),zipFile)
    println("wrote out " + zipFile.getAbsolutePath)

    // return the File .. (name and the full path to it)
    return (dbZipFilename,zipFile)
  }

    /**
     * We will only allow underscores and hyphens (no dots or dasshes
     * @param filename
     * @return
     */
  def sanitiseFileName(filename: String): String = filename.replaceAll("[^A-Za-z0-9_-]", "_");

    /**
     * We only allow alpha and numeric with an underscore
     * @param filename
     * @return
     */
  def sanitiseDBName(filename: String): String = filename.replaceAll("[^A-Za-z0-9_]", "_");

}


