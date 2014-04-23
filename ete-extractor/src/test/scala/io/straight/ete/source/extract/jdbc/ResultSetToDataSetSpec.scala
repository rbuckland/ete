package io.straight.ete.source.extract.jdbc

import org.scalatest._
import java.sql.ResultSet
import org.scalatest.mock.EasyMockSugar
import org.scalatest.matchers.ShouldMatchers
import io.straight.ete.config._
import java.io.{FileOutputStream, PrintWriter, File}
import io.straight.ete.source.extract.EteExecutor
import io.straight.ete.config.SimpleJdbcSourceData
import io.straight.ete.config.XlsSourceData
import scala.Some
import org.jasypt.intf.service.JasyptStatelessService
import org.jasypt.util.text.StrongTextEncryptor
import scalaz.Tree
import io.straight.ete.config.EteConfigBuildSupport._
import io.straight.ete.config.SqlStatement
import io.straight.ete.config.SimpleJdbcSourceData
import io.straight.ete.config.SimpleNode
import io.straight.ete.config.DataSetAttachedNode

/**
 * This spec tests that we get a valid looking dataset from a JDBC resultset
 *
 * @author rbuckland
 */
class ResultSetToDataSetSpec extends FlatSpec with EasyMockSugar with ShouldMatchers {
//  val textEncryptor = new StrongTextEncryptor()
//  val passwordString = textEncryptor.encrypt("sa")

  "ete jdbcExtraction" should "create a dataset from a resultSet" in {

    val jdbc = SimpleJdbcSourceData(
      dataSetId = "sample",
      sqlStatements = Vector(
        SqlStatement("SELECT * FROM products"),
        SqlStatement("SELECT * FROM data")
      ),
      jdbcDriver = "org.h2.Driver",
      jdbcUrl = "jdbc:h2:file:ete-extractor/src/test/resources/sample_database;IFEXISTS=TRUE"
//      username = "sa",
//      password = Some(passwordString)
    )

    val dataDataSet = DataSetAttachedNode(
      name = "data",
      dataSetId = "sample-1")

    val productDataSet = DataSetAttachedNode(
      name = "product",
      dataSetId = "sample-0"
    )

    val rootNode = Tree.node(aio(SimpleNode("rootNode")),Stream(Tree.node(aio(productDataSet),Stream(Tree.leaf(aio(dataDataSet))))))

    val config = EteConfig.createNew("foobar", rootNode, Vector(jdbc))

    val resultsDir = new File("target/test-results/")
    resultsDir.mkdirs()

    println(EteConfigBuildSupport.treeToString(config.rootMapping))

    Vector("json", "xml").foreach(t => {
      val outFile = new File(resultsDir, "jdbcMappingSpec_sampleout." + t)
      val outs = new FileOutputStream(outFile)
      EteExecutor.generate(config, outs, Vector.empty, t)
      outs.flush()
      println("wrote " + t + " results to " + outFile.getAbsoluteFile)
    }
    )

  }
}
