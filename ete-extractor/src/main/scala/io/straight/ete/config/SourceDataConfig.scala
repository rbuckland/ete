package io.straight.ete.config

import javax.sql.DataSource
import java.io.{InputStream, File}

/**
 * @author rbuckland
 */
abstract class SourceDataConfig(val dataSetId:String)


// --------------
// JDBC type DataSources
// --------------
abstract class JdbcSourceData(
                           override val dataSetId:String,
                           val datasource: Option[(String,DataSource)],
                           val sqlStatements: Vector[SqlStatement]
                           ) extends SourceDataConfig(dataSetId)

case class JdbcDSPresetSourceData(
                               override val dataSetId:String,
                               override val datasource: Option[(String,DataSource)],
                               override val sqlStatements: Vector[SqlStatement]
                               ) extends JdbcSourceData(dataSetId,datasource,sqlStatements) {

  override def toString = getClass.getCanonicalName + s"($myName)"

  lazy val myName = datasource match {
    case None => "datasource#notset"
    case Some(ds) => ds._1
  }

}

case class JndiJdbcSourceData(
                           override val dataSetId:String,
                           override val datasource: Option[(String,DataSource)],
                           override val sqlStatements: Vector[SqlStatement],
                             jndiUrl: String
                           ) extends JdbcSourceData(dataSetId,datasource,sqlStatements) {
  override def toString = getClass.getCanonicalName + "(" + jndiUrl + ")"
}

case class SimpleJdbcSourceData(
                               override val dataSetId:String,
                               override val datasource: Option[(String,DataSource)],
                               override val sqlStatements: Vector[SqlStatement],
                               jdbcDriver: String, // eg "org.h2.Driver"
                               jdbcUrl: String
                               ) extends JdbcSourceData(dataSetId,datasource,sqlStatements) {
  override def toString = getClass.getCanonicalName + "(" + jdbcUrl + ")"
}

case class SimpleUserPassJdbcSourceData(
                                 override val dataSetId:String,
                                 override val datasource: Option[(String,DataSource)],
                                 override val sqlStatements: Vector[SqlStatement],
                                 jdbcDriver: String, // eg "org.h2.Driver"
                                 jdbcUrl: String,
                                 username: String,
                                 password: Option[String]
                                 ) extends JdbcSourceData(dataSetId,datasource,sqlStatements) {
  override def toString = getClass.getCanonicalName + "(" + jdbcUrl + ";username=" + username + ")"
}

case class SqlStatement(sqlString: String, sqlParameters: Vector[AnyRef] = Vector.empty)

/**
 * An XlsSourceData config object.
 *
 * @param dataSetId the dataSetId specific for this XLS
 * @param file the file where we will find the XLS
 * @param sourceName the name of the Source
 * @param sheetName
 * @param rowRestrictor
 */
case class XlsSourceData(
                          override val dataSetId:String,
                          file: File, // the other form could be an Array[Byte] .. but need to look at POI as it
                                      // performs better when reading from File
                                      // http://poi.apache.org/spreadsheet/quick-guide.html#FileInputStream
                          sourceName: String,
                          sheetName: Option[String],
                          rowRestrictor: Option[String]
                          ) extends SourceDataConfig(dataSetId)

/**
 * We will accept CSV data as a File, maybe later just a String
 *
 * @param dataSetId
 * @param csvFile
 * @param sourceName
 * @param sheetName
 * @param rowRestrictor
 */
case class CsvSourceDataConfig(
           override val dataSetId:String,
           csvFile: File,
           sourceName: String,
           sheetName: Option[String],
           rowRestrictor: Option[String]) extends SourceDataConfig(dataSetId)
