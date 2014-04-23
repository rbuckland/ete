package io.straight.ete.source.extract.xls

import io.straight.ete.source.data._
import org.apache.poi.hssf.usermodel.{HSSFWorkbook, HSSFSheet}
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import java.io.{InputStream, FileInputStream}
import org.apache.poi.ss.usermodel.{Cell, Row}
import scala.Predef._
import io.straight.ete.config.{EteConfig, OutputNode, XlsSourceData}
import org.slf4j.LoggerFactory
import io.straight.ete.source.data
import scala.Some
import io.straight.ete.source.data.DataRow
import io.straight.ete.util.EteHelper._

/**
 * @author rbuckland
 */
object XlsSourceExtractor {

  val logger = LoggerFactory.getLogger(getClass)
  /**
   * Take in  the xlsSourceData and turn it into a DataSet, and put it in the DataSetHolder
   * @param xlsSourceData
   * @param dataSetHolder
   */
  def extractXlsData(config: EteConfig, xlsSourceData:XlsSourceData, dataSetHolder: DataSetHolder, indexingFunction: (Int,DataRow,DataSet) => DataRow):String = {

    val sheet = XlsHelper.loadSheet(xlsSourceData.file, xlsSourceData.sheetName)
    val dataSet = dataSetHolder.newDataSet(
      XlsHelper.deriveHeaders(xlsSourceData.sourceName,sheet)
      ,xlsSourceData.dataSetId)

    applyColNamesToPrimaryKeys(config.rootMapping,dataSet.dataSetId, dataSet.dataRowColumnNames.keySet)

    // oh so concicse, for every row, create a rowData object.
    // and have all DataRows into a Vector
    for (
      i <- 1 until sheet.getLastRowNum;
      row = sheet.getRow(i);
      rowData = (0 until row.getLastCellNum).map( x => XlsHelper.valueFromCell(row.getCell(x)) ).toVector
    ) yield {
      dataSet.addDataRow(indexingFunction(i-1,DataRow(rowData),dataSet))
    }

    logger.info("Read " + dataSet.size + " rows(s) to " + dataSet.dataSetId + " from " + xlsSourceData.sourceName + "!" + sheet.getSheetName)

    return "Complete"
  }



}





