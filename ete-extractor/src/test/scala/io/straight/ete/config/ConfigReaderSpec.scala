package io.straight.ete.config

import org.scalatest._
import org.scalatest.matchers._
import java.io.{InputStreamReader, Reader, FileInputStream, File}
import org.xml.sax.InputSource
import javax.xml.xpath.XPathFactory

/**
 * @author rbuckland
 */
class ConfigReaderSpec extends FlatSpec with ShouldMatchers {

  "reading a simple xml config" should "produce no errors" in {

    val xmlFile = new File("src/test/resources/io/straight/ete/config/sample-config.xml")
    val inputSource = new InputSource(new InputStreamReader(new FileInputStream(xmlFile),"UTF-8"))
    val config = EteConfig.build(inputSource)

    config.rootMapping should not be (null)
    println(EteConfigBuildSupport.treeToString(config.rootMapping))

    // TODO write the tree loc correctly.
    config.rootMapping.loc.find { case x : DataSetAttachedNode  => x.name == "option"}.isDefined should be (true)

  }


}
