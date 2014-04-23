package io.straight.ete.source.data.indexing

import org.scalatest._
import io.straight.ete.config.{OutputNode, DataSetAttachedNode}
import org.scalatest.matchers.ShouldMatchers

/**
 * @author rbuckland
 */
/*
class IndexingSpec extends FlatSpec with ShouldMatchers {

  val childNode = DataSetAttachedNode(name = "childNode",dataSetId="subChildDataSet1")
  val parentNode = DataSetAttachedNode(name = "parentNode", dataSetId = "parentDataSet0")

  "a simple node output map with two dataSetAttachedNodes" should "produce a set of keys to be indexed from the child only" in {

    childNode.initialisePrimaryKeys(Vector("child_id"))
    parentNode.initialisePrimaryKeys(Vector("parent_id","composite_parent_id"))
    val indexingKeys = IndexMaster.primaryKeysForIndexing(parentNode)

    println("indexing keys are " + indexingKeys)

    indexingKeys.get("subChildDataSet1").size should equal (1) // one tuple
    indexingKeys.get("subChildDataSet1").get(0).size should equal (2) // two elements (parent keys)
    indexingKeys.get("subChildDataSet1").get(0)(0) should equal ("parent_id") // two elements (parent keys)
    indexingKeys.get("subChildDataSet1").get(0)(1) should equal ("composite_parent_id") // two elements (parent keys)
  }


}
*/
