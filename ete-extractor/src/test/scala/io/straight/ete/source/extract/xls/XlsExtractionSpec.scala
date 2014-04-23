package io.straight.ete.source.extract.xls

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import io.straight.ete.config.{DataSetAttachedNode, XlsSourceData, SimpleNode, EteConfig}
import java.io.{File, FileInputStream, StringWriter}
import io.straight.ete.source.data.{InMemoryDataSetHolder, InMemoryDataSet, DataSetHolder}
import io.straight.ete.source.data.indexing.IndexMaster
import org.scalatest.matchers.ShouldMatchers
import scalaz.Tree
import io.straight.ete.config.EteConfigBuildSupport._
import io.straight.ete.config.SimpleNode
import io.straight.ete.config.XlsSourceData
import scala.Some
import io.straight.ete.config.DataSetAttachedNode

/**
 * @author rbuckland
 */
class XlsExtractionSpec extends FlatSpec with ShouldMatchers {


  "extracting data from a basic XLS" should "generate a DataSet" in {


    // this test is quite complex, which perhaps explains that the underlying structure is quote complex
    // so we perhaps need to refactor somewhet .. but for now..
    val dsh = new InMemoryDataSetHolder()
    val mapping = DataSetAttachedNode(name = "myNode", dataSetId = "sampleXls")

    val rootNode = Tree.node(aio(SimpleNode("rootNode")),Stream(Tree.leaf(aio(mapping))))

    val xlsSrcData = XlsSourceData("sampleXls",
      new File(Thread.currentThread().getContextClassLoader.getResource("MOCK_DATA.xls").getFile),"MOCK_DATA.xls",Some("data"),None)
    XlsSourceExtractor.extractXlsData(
         EteConfig(name="sampleconfig",rootMapping=rootNode,sources=Vector(xlsSrcData))
        ,xlsSrcData
        ,dsh
      , IndexMaster.createIndexFunction(rootNode))

    dsh.dataSets.size should be (1)

  }

  "XLS with a formula as column" should "still give us the cell's value as the key (column name)" in {

    // the XLS has a column called "col_name_from_formula_otherColumn" that is derived from a formula

    val dsh = new InMemoryDataSetHolder()
    val mapping = DataSetAttachedNode(name = "myNode", dataSetId = "sampleXls")

    val rootNode = Tree.node(aio(SimpleNode("rootNode")),Stream(Tree.leaf(aio(mapping))))

    val xlsSrcData = XlsSourceData("sampleXls",
      new File(Thread.currentThread().getContextClassLoader.getResource("MOCK_DATA.xls").getFile),"MOCK_DATA.xls",Some("test_colnames"),None)
    XlsSourceExtractor.extractXlsData(
      EteConfig(name="sampleconfig",rootMapping=rootNode,sources=Vector(xlsSrcData))
      ,xlsSrcData
      ,dsh
      , IndexMaster.createIndexFunction(rootNode))

    dsh.dataSets.size should be (1)
    dsh.dataSets.get("sampleXls-0").get.dataRowColumnNames.keySet.contains("col_name_from_formula_otherColumn") should be (true)
    println(dsh.dataSets.get("sampleXls-0").get.dataRowColumnNames)

  }

}