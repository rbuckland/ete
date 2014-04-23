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

  val config = EteConfig.createNew("foobar",rootNode,Vector(xlsSrcData1,xlsSrcData2))

  "mapping to lists of objects with a key of id" should "generate the correct Json" in {


    val resultsDir = new File("target/test-results/")
    resultsDir.mkdirs()

    println(EteConfigBuildSupport.treeToString(config.rootMapping))

    Vector("json","xml").foreach( t =>
      {
        val outFile = new File(resultsDir,"xlsMappingSpec_sampleout." + t)
        val out = new FileOutputStream(outFile)
        EteExecutor.generate(config,out,Vector.empty,t)
        println("wrote " + t + " results to " + outFile.getAbsoluteFile)
      }
    )

  }

}