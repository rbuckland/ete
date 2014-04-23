package io.straight.ete.source.extract.csv

import org.apache.poi.hssf.usermodel.HSSFSheet
import io.straight.ete.source.data
import org.apache.poi.ss.usermodel.Row
import java.io.{StringReader, File, FileReader}
import org.apache.commons.csv.CSVFormat
import scala.collection.immutable.Range.Int
import io.straight.ete.source.data.DataRowNameMapping

/**
 * Take a look at : https://gist.github.com/ArtemGr/115557
 * @author rbuckland
 */
object CsvHelper {

  /**
   * Work through  the first row found in the spreadsheet and
   * extract out the column names (and their "number") to a Map
   *
   * @param csvFile
   * @return
   */
  def deriveHeaders(csvFile: File):data.DataRowNameMapping = {
    import scala.collection.JavaConverters._
    val reader = new FileReader(csvFile)

    // TODO support the other CSV types supported by apache commons cSV
    return CSVFormat.DEFAULT.parse(reader)
      .getHeaderMap.asScala.map{ case (k,v) =>  (k:String) -> (v:Int) }.asInstanceOf[DataRowNameMapping]
  }

  def deriveHeaders(csvData: String):data.DataRowNameMapping = {
    import scala.collection.JavaConverters._
    val reader = new StringReader(csvData)

    // TODO support the other CSV types supported by apache commons cSV
    return CSVFormat.DEFAULT.parse(reader)
      .getHeaderMap.asScala.map{ case (k,v) =>  (k:String) -> (v:Int) }.asInstanceOf[DataRowNameMapping]
  }



}
