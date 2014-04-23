package io.straight.ete.source.extract.csv

import io.straight.ete.config.{CsvSourceDataConfig, SourceDataConfig, EteConfig}
import io.straight.ete.source.data.{DataSet, DataRow, DataSetHolder}
import io.straight.ete.source.extract.SourceExtractor
import io.straight.ete.util.EteHelper._
import io.straight.ete.source.data.DataRow
import io.straight.ete.config.CsvSourceDataConfig
import org.slf4j.LoggerFactory

/**
 * @author rbuckland
 */
object CsvSourceExtractor extends SourceExtractor[CsvSourceDataConfig] {

  val logger = LoggerFactory.getLogger(getClass)

  def extractData(config: EteConfig, sourceData:CsvSourceDataConfig, dataSetHolder: DataSetHolder, indexingFunction: (Int,DataRow,DataSet) => DataRow) : String = {

    val dataSet = dataSetHolder.newDataSet(CsvHelper.deriveHeaders(sourceData.csvFile),sourceData.dataSetId)
    applyColNamesToPrimaryKeys(config.rootMapping,dataSet.dataSetId, dataSet.dataRowColumnNames.keySet)

    // oh so concicse, for every row, create a rowData object.
    // and have all DataRows into a Vector
//    for (
//      i <- 1 until sheet.getLastRowNum;
//      row = sheet.getRow(i);
//      rowData = (0 until row.getLastCellNum).map( x => XlsHelper.valueFromCell(row.getCell(x)) ).toVector
//    ) yield {
//      dataSet.addDataRow(indexingFunction(i-1,DataRow(rowData),dataSet))
//    }

    logger.info("Read " + dataSet.size + " rows(s) to " + dataSet.dataSetId + " from " + sourceData.sourceName)
    return "Complete"
  }
}
