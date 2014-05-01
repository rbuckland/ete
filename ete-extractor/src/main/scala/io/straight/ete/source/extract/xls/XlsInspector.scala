package io.straight.ete.source.extract.xls

import java.io.File
import io.straight.ete.config._
import org.apache.poi.hssf.usermodel.HSSFSheet
import io.straight.ete.core.EteGeneralException
import org.slf4j.LoggerFactory
import scalaz.Tree
import io.straight.ete.config.EteConfigBuildSupport._
import io.straight.ete.config.XlsSourceData
import scala.Some
import io.straight.ete.config.SimpleNode
import io.straight.ete.config.DataSetAttachedNode

/**
 *
 * This could be so much cleaner.. and more concise
 *
 * @author rbuckland
 */
object XlsInspector {

  val logger = LoggerFactory.getLogger(getClass)

  /**
   * In some instances, the filename is a random string and we may know what the file
   * originally was
   *
   * @param file
   * @param filename
   * @return
   */
  def createConfig(file: File, filename: String): EteConfig = {

    val sources = determineDataSources(file, filename)

    val dataSetsWithColumns = {for (
      source <- sources;
      dataSetId:String = source.dataSetId;
      columnNames:Vector[String] = XlsHelper.deriveHeaders(filename, XlsHelper.loadSheet(file,source.sheetName)).keySet.toVector
    ) yield dataSetId -> columnNames }.toList

    // we now have a list of Vector[(dataSetIds -> columnNames)]
    // we are going to nest these
    val rootNode = Tree.node(
                 aio(SimpleNode(EteConfig.DEFAULT_OUTPUT_NODE)),
                 createSubForestOfDataSetAttachedNodes(dataSetsWithColumns, Vector.empty)
               )
    return EteConfig(name="defaultInspectedConfig",rootMapping = rootNode,sources)

  }

  def determineDataSources(file: File, filename: String): Vector[XlsSourceData] = {
    val workbook = XlsHelper.loadWorkbook(file)

    val xlsSources = for (
      i <- 0 to workbook.getNumberOfSheets.toInt - 1;
      sheet: HSSFSheet = workbook.getSheetAt(i)
    ) yield XlsSourceData(sheet.getSheetName,file,filename,Some(sheet.getSheetName), None)


    xlsSources.toVector
  }

}

case class XlsConfigAutoBuildError(message: String) extends EteGeneralException(message)
