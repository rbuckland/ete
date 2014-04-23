package io.straight.ete.source.data

import scala.collection.mutable

/**
 * @author rbuckland
 */
case class DataRow(row: Vector[Any]) {

  /**
   * Get a value by columnName
   * @param colName
   * @return
   */
  def value(colName: String, dataRowColumnNames: DataRowNameMapping) : Any = row(dataRowColumnNames(colName))

  def size = row.size

}
