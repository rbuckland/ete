package io.straight.ete.config

import org.scalatest._
import org.scalatest.matchers._
import java.io.{InputStreamReader, Reader, FileInputStream, File}
import org.xml.sax.InputSource
import javax.xml.xpath.XPathFactory
import scala.io.Source
import scalaz.Tree

/**
 * @author rbuckland
 */
class ConfigReaderSpec extends FlatSpec with ShouldMatchers {

  "reading a simple xml config" should "produce no errors" in {

    val xmlFile = "io/straight/ete/config/sample-config.xml"
    val inputSource = new InputSource(new InputStreamReader(getClass.getClassLoader.getResourceAsStream(xmlFile),"UTF-8"))
    val config = EteConfig.build(inputSource)

    config.rootMapping should not be (null)
    config.name should equal ("myBasicConfig")

    println(EteConfigBuildSupport.treeToString(config.rootMapping))

    config.rootMapping.flatten.filter{ p => p.isInstanceOf[DataSetAttachedNode] && p.asInstanceOf[DataSetAttachedNode].name == "option"}.size should be > 0


  }


}
