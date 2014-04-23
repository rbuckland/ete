package io.straight.ete.source.data

import org.scalatest._
import io.straight.ete.config.DataSetAttachedNode
import org.scalatest.matchers.ShouldMatchers


/**
 * @author rbuckland
 */
class PrimaryKeyCollectionSpec extends FlatSpec with ShouldMatchers {

  val columnNameMap = Map[Int,String](0 -> "foo", 1 -> "person_id", 2 -> "pet_name",3 -> "pet_breed")
  "a node with no primary keys" should "return a set of primary keys if there is 'id' column" in {
    val dsNode = new DataSetAttachedNode( name = "node", dataSetId = "mydataset")
    val dsNode2 = dsNode.initialisePrimaryKeysFromColumnNames(columnNameMap.values.toSet)
    dsNode2.primaryKeys.head should equal ("person_id")

  }

  "a node with a primary key set" should "return that primary key" in {
    val dsNode = new DataSetAttachedNode( name = "node", dataSetId = "mydataset")
    val dsNode2 = dsNode.initialisePrimaryKeysFromColumnNames(Set("foo"))
    dsNode2.primaryKeys.head should equal ("foo")

  }


}
