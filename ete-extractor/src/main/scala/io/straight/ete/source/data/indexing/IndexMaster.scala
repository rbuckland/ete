package io.straight.ete.source.data.indexing

import io.straight.ete.source.data.{DataSet, PrimaryKeys, DataRow}
import io.straight.ete.config.{ContainerNode, DataSetAttachedNode, OutputNode}

import scalaz._
import Scalaz._
import scala.collection.parallel.mutable
import org.slf4j.LoggerFactory

/**
 * @author rbuckland
 */
object IndexMaster {

  val logger = LoggerFactory.getLogger(getClass)

  def createIndexFunction(rootMapping: Tree[OutputNode]): (Int,DataRow,DataSet) => DataRow = {

    // primary keys for each dataSetId inside the mapping

    val keysForIndexing = primaryKeysForIndexing(rootMapping)

    /**
     * Index the dataRow
     */
    def indexer(rowNumber: Int, dataRow: DataRow, dataSet: DataSet): DataRow = {

      // quick exit check .. The root mappings don't need indexing (until we add where or limiter config)

      val index = dataSet.index

      val indexKeys = keysForIndexing.get(dataSet.dataSetId) match {
        case None => return dataRow // no indexing required
        case Some(vectorOfPriKeyTuples) =>
        vectorOfPriKeyTuples.map{ colNameSet: Vector[String] =>
          colNameSet.map( colName => (
            // the column name
            colName,
            // the value
            dataRow.row(dataSet.dataRowColumnNames.getOrElse(colName,-1))
            )
          )
        }
      }


      indexKeys.foreach( idxKey =>  {
                                      logger.trace("index entry " + idxKey + "[" + (idxKey ##) + "] row " + rowNumber)
                                      index.getOrElseUpdate(idxKey ##,mutable.ParHashSet.empty).par += rowNumber
                                    }
      )

      return dataRow
    }

    return indexer
  }



  /**
   * What keys are being used for indexing ?
   *
   * @param tree
   * @return
   */
  def primaryKeysForIndexing(tree: Tree[OutputNode], parentPrimaryKeys: Vector[String] = Vector.empty): Map[String,Vector[Vector[String]]] = {

    // the keys that need to be indexed on a sub "dataset" are the parent dataset keys
    val thisNodeKeys: Map[String,Vector[Vector[String]]] = tree.rootLabel match {
      case d: DataSetAttachedNode if ! parentPrimaryKeys.isEmpty => Map(d.dataSetId -> Vector(parentPrimaryKeys))  // TODO support remapping
      case _ => Map.empty
    }

    val childNodeKeys: Map[String,Vector[Vector[String]]] = tree.rootLabel match {
      case d: DataSetAttachedNode => {
        val treeZipper = tree.loc // we grab the iter so we can call hasNext to see if we are the last
        treeZipper.rights.foldLeft(Map.empty[String, Vector[Vector[String]]])((a, c) => a |+| primaryKeysForIndexing(c, d.primaryKeys))
      }
      case p: ContainerNode => {
        val treeZipper = tree.loc // we grab the iter so we can call hasNext to see if we are the last
        treeZipper.rights.foldLeft(Map.empty[String, Vector[Vector[String]]])((a, c) => a |+| primaryKeysForIndexing(c, parentPrimaryKeys))
      }
      case _ => Map.empty
    }

    thisNodeKeys |+| childNodeKeys
  }


}
