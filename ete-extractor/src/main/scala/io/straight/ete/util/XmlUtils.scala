package io.straight.ete.util

import org.xml.sax.InputSource
import org.w3c.{ dom => w3cd }
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.namespace.NamespaceContext
import javax.xml.XMLConstants

/**
 * @author rbuckland
 */
object XmlUtils {

  import io.straight.ete.config.EteConstants._

  /*
   * Our NameSpace Context Handler
   */
  val NsContext = new NamespaceContext {
    import scala.collection.JavaConverters._

    def getNamespaceURI(prefix: String) = prefix match {
      case ETE_NS_PREFIX => ETE_NS
      case _ => XMLConstants.NULL_NS_URI
    }
    def getPrefix(namespaceURI: String) = namespaceURI match {
      case ETE_NS => ETE_NS_PREFIX
      case _ => null
    }
    def getPrefixes(namespaceURI: String):java.util.Iterator[String] = List(getPrefix(namespaceURI)).iterator.asJava

  }

  /*
   * Our XPath Handler
   */
  val xpath = {
    val xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath()
    xpath.setNamespaceContext(NsContext)
    xpath
  }

  def loadAsDocument(inputSource: InputSource):w3cd.Document = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(true)
    // factory.setValidating(true)
    // TODO
    // factory.setSchema()
    factory.setIgnoringElementContentWhitespace(true)
    val builder = factory.newDocumentBuilder()
    return builder.parse(inputSource)
  }

  def getNodes(node: w3cd.Node, expression:String) : w3cd.NodeList = {
    xpath.evaluate(expression, node, XPathConstants.NODESET).asInstanceOf[w3cd.NodeList]
  }

  def getNode(node: w3cd.Node, expression:String) : w3cd.Node = {
    xpath.evaluate(expression, node, XPathConstants.NODE).asInstanceOf[w3cd.Node]
  }

}
