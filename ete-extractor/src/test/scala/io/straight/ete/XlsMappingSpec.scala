package io.straight.ete

import org.scalatest.FlatSpec

import io.straight.ete.config._
import java.io.{FileOutputStream, PrintWriter, File}
import io.straight.ete.config.XlsSourceData
import io.straight.ete.config.SimpleNode
import scala.Some
import io.straight.ete.source.extract.EteExecutor
import org.scalatest.matchers.ShouldMatchers

/**
 * @author rbuckland
 */
class XlsMappingSpec extends FlatSpec with ShouldMatchers {

/*
  val dataDataSet = DataSetAttachedNode(
                name = "child",
                dataSetId = "superSample-1")
  val productDataSet = DataSetAttachedNode(
                name = "parent",
                dataSetId = "superSample-0"
                )
                */

  val rootNode = EteConfigBuildSupport.createBasicDefaultTree(dataSetsToPKs = List("superSample" -> Vector("foo_id")))

  val testFile = new File(Thread.currentThread().getContextClassLoader.getResource("1sheet-sample.xls").getFile)
  val xlsSrcData1 = XlsSourceData("superSample",testFile,testFile.getAbsoluteFile.toString,Some("products"),None)
  val xlsSrcData2 = XlsSourceData("superSample",testFile,testFile.getAbsoluteFile.toString,Some("data"),None)
  val xlsSrcData3 = XlsSourceData("superSample",testFile,testFile.getAbsoluteFile.toString,Some("widget"),None)

  val xmlConfig =
    <ete:config xmlns:ete="http://io.straight/ete" name="myBasicConfig">

      <ete:source name="products" type="xlsx" uri="file://src/test/resources/1sheet-sample.xls">
        <ete:sheetname>products</ete:sheetname>
        <ete:rowrange>1-20</ete:rowrange>
      </ete:source>

      <ete:source name="data" type="xlsx" uri="file://src/test/resources/1sheet-sample.xls">
        <ete:sheetname>data</ete:sheetname>
      </ete:source>

      <ete:source name="prices" type="sql" uri="jdbc:h2:file:src/test/support/sample-db">
        <ete:sql>select * from prices</ete:sql>
      </ete:source>

      <ete:output>
        <products>
          <ete:per-row name="product" source="products">
            A Test of some Text
            <options>
              <ete:per-row name="option" source="options"/>
            </options>
          </ete:per-row>
        </products>
      </ete:output>

    </ete:config>


  val config = EteConfig.createEteConfig("foobar",rootNode,Vector(xlsSrcData1,xlsSrcData2))

  "mapping to lists of objects with a key of id" should "generate the correct Json" in {


    val resultsDir = new File("target/test-results/")
    resultsDir.mkdirs()

    println(EteConfigBuildSupport.treeToString(config.rootMapping))

    Vector("json","xml").foreach( t =>
      {
        val outFile = new File(resultsDir,"xlsMappingSpec_sampleout." + t)
        val out = new FileOutputStream(outFile)
        EteExecutor.generate(config,out,Vector.empty,t, true)
        println("wrote " + t + " results to " + outFile.getAbsoluteFile)
      }
    )

    // we are going to assert values inside the XML file

    // this is some data inside the XLS (three sheets) products, data, widget
    val xpathTest = "//products[foo_id = 'foo 3']/data[data_id = '2']/widget[widget_id = '14']/meta_description"

  }

}