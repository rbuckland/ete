package io.straight.ete.source.data

import scala.collection.mutable
import scala.collection.parallel.mutable.ParHashSet

/**
 * The Dataset name will be from the "datasource"
 * The ID would be incremental if the datasource returned more than one
 *
 * The data set needs two names.. the dataSetName will always match the config name + the result Set Number
 *
 * @author rbuckland
 *
 */
trait DataSet {

  val dataRowColumnNames: DataRowNameMapping

  /**
   * Swap the mapping around because we need that when rendering a row
   */
  lazy val dataRowIdxToColumnNames = dataRowColumnNames map { _.swap }

  /**
   * Add a new row to the dataRows Object
   * @param dataRow
   */
  def addDataRow(dataRow: DataRow): Unit

  def dataRowsIterator: Iterator[DataRow]

  def getDataRow(rowNumber: Int): DataRow

  /**
   * Size of the dataset (number of rows)
   * @return
   */
  def size: Int


  /**
   * The name that we end up with
   */
  var dataSetId: String = null

  /**
   * Our row index
   */
  val index = new mutable.HashMap[Int, ParHashSet[Int]]

  /**
   * Return an iterator that uses the index
   * @param indexKey
   * @return
   */
  def dataRowIterator(indexKey: Option[IndexKey]): Iterator[DataRow] = {
    indexKey match {
      case None => dataRowsIterator
      case Some(indexKey) => {
          case class IndexedIterator(indexKey: IndexKey) extends Iterator[DataRow] {
            val rowNumberIterator = try {
                                          index.getOrElse(indexKey ##,ParHashSet[Int]()).iterator
                                        } catch {
                                          case e: NoSuchElementException =>
                                            throw new DataSetException("Could not find key ["
                                              + indexKey + "] in " + dataSetId)
                                        }
            def hasNext = rowNumberIterator.hasNext
            def next = getDataRow(rowNumberIterator.next())
          }
          IndexedIterator(indexKey)
      }
    }
  }
}
