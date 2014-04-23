package io.straight.ete.source.data

import scala.collection.mutable.ArrayBuffer

/**
 * Simple non cached version
 * @author rbuckland
 */
case class InMemoryDataSet(dataRowColumnNames: DataRowNameMapping) extends DataSet {
  private var dataRows: ArrayBuffer[DataRow] = ArrayBuffer.empty[DataRow]
  def addDataRow(dataRow: DataRow): Unit = dataRows += dataRow
  def size: Int = dataRows.size
  def dataRowsIterator: Iterator[DataRow] = dataRows.iterator
  def getDataRow(rowNumber: Int): DataRow = dataRows(rowNumber)

  // testing helper
  def addAllDataRows(newDataRows: Iterable[DataRow]) = dataRows ++= newDataRows

}
