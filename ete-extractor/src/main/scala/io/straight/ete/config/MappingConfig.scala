package io.straight.ete.config

import org.slf4j.LoggerFactory
import io.straight.ete.source.data._

/**
 * @author rbuckland
 */

/**
 * The basic typs of nodes we can have in the configuration
 */
sealed abstract class OutputNode
/**
 * Just a free format node that has no children
 * @param text
 */
case class TextNode(text:String) extends OutputNode

/**
 * Container Node - Important difference between this and the Text node is the
 * TextNode does not have a name (only Text)
 * All ContainerNode Nodes must have a name
 *
 * @param name
 */
abstract class ContainerNode(val name: String) extends OutputNode

/**
 * A node that can repeat it's contents and children based on a dataset
 * The primary keys is purposefully set as mutable so we can lazily add the primary keys
 * after we iterate the first row. (this could bite.. might be ok)
 *
 * -- I tried a more hard code memo-isation of the result as a function returned reference
 * but this failed to not "iterate" the vector somewhere . complex
 *
 * @param name
 * @param dataSetId
 */
case class DataSetAttachedNode(
  override val name: String,
  dataSetId: String,
  primaryKeys: Vector[String] = Vector.empty
) extends ContainerNode(name) {

  val logger = LoggerFactory.getLogger(classOf[DataSetAttachedNode])

  /**
   * Returns a copy of the DataSetAttachedNode with the primaryKeys set
   *
   * @param columnNames
   * @return
   */
  def initialisePrimaryKeysFromColumnNames(columnNames : Set[String]): DataSetAttachedNode = {

    /*
     * We want the primary keys listed on the outputNode.
     * If the outputNode has declared "no" primary keys then we will
     * get the first "id" column, or if not found, the first column
     *
     */
    if (!columnNames.isEmpty) {
      // take the first column that ends in "id" or take the first column if no "id" is found
      val priKeys = Option(columnNames.filter(_.toLowerCase.endsWith("id")).head) match {
        case Some(idPriKey) => Vector(idPriKey)
        case None => Vector(columnNames.head)
      }
      logger.info("Column names provided. Selected primary key as " + priKeys)
      copy( primaryKeys = priKeys)
    } else {
      this
    }
  }

  /**
   *   If the nodes are aa, bb, cc mapping to ds1,ds2,ds3 and the keys are d, (e,f) and g
   * aa ds1  (d)
   *   bb ds2 (e,f)
   *     cc ds3 (g)
   *
   * These are then the mappings
   *     ds1= ?? none
   *     ds2={d}
   *     ds3={e,f}
   *
   * TODO If we later add where clause restrictions then we will need to index the where clause on that datasource
   *
   */
  @deprecated("this has been implemented another way", "Tuesday")
  def keysForIndexing(parentNode: ContainerNode): Map[String,Vector[Vector[String]]] = Map(dataSetId -> parentIndexingKeys(Option(parentNode)))

  /**
   * Find the parent keys (first ones) these will be out dataSetId keys
   */
  private def parentIndexingKeys(parentNode: Option[ContainerNode]):Vector[Vector[String]] = {

    if (parentNode.isEmpty) return Vector.empty

    parentNode.get match {
      case d: DataSetAttachedNode => Vector(d.primaryKeys)
     // case o: ContainerNode => parentIndexingKeys(Option(o.parentNode)) // TODO need to work this out
      case _ => Vector.empty
    }
  }

  /**
   * An IndexKey is a Vector of (String,Any)
   * @param dataRow
   * @return
   */
  def indexKeyFromRow(dataRow: DataRow, dataRowColumnNames: DataRowNameMapping): IndexKey = {
    primaryKeys.map { key =>
      key -> dataRow.value(key, dataRowColumnNames)
    }
  }

}

/**
 * A Simple node is just an JSON Static definition or an XML Element all by itself.
 *
 * TODO In the case of XML we have extra attributes (namespaces etc)
 * @param name
 */
case class SimpleNode(override val name: String) extends ContainerNode(name)
