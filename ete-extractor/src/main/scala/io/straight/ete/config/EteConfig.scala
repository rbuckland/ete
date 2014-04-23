package io.straight.ete.config

import org.w3c.{ dom => w3cd }
import io.straight.ete.core.EteGeneralException
import javax.xml.namespace.NamespaceContext
import javax.xml.XMLConstants
import org.xml.sax.InputSource
import javax.xml.xpath.XPathConstants
import javax.xml.parsers.DocumentBuilderFactory
import org.slf4j.LoggerFactory
import EteConfigBuildSupport._
import scalaz._
import Scalaz._

/**
 * @author rbuckland
 */
case class EteConfig(name: String, rootMapping: Tree[OutputNode], sources: Vector[SourceDataConfig])

object EteConfig {

  val logger = LoggerFactory.getLogger(getClass)
  val ETE_NS = "http://io.straight/ete"
  val ETE_NS_PREFIX = "ete"
  val eteNs = scala.xml.NamespaceBinding("ete",ETE_NS,null)
  val DEFAULT_OUTPUT_NODE = "data"


  /**
   * Create a Config from an XML File.
   * @param xmlInputSource
   * @return
   */
  def build(xmlInputSource: InputSource) : EteConfig = build(loadAsDocument(xmlInputSource))

  /**
   * Traditional Config Builder
   *
   * @return
   */
  def build(configXml: w3cd.Document) : EteConfig = {
    val configName = xpath.evaluate("/ete:config/@name",configXml)
    val outputNode = xpath.evaluate("/ete:config/ete:output",configXml,XPathConstants.NODE).asInstanceOf[org.w3c.dom.Node]
    return createNew("name",createTreeFromXml(outputNode),null)
  }

  /**
   * A Builder method for making an eteConfig
   * @param name
   * @param rootMapping
   * @param sources
   * @return
   */
  def createNew(name: String, rootMapping: Tree[OutputNode], sources: Vector[SourceDataConfig]):EteConfig = {

    // we need to check and make sure the root mapping is a simple node.. it always has to be
    val actualRootMapping: Tree[OutputNode] = rootMapping match {
      case Tree.Node(SimpleNode(_),_) => rootMapping
      case _ => aio(SimpleNode(DEFAULT_OUTPUT_NODE)).node(rootMapping)
    }
    return EteConfig(name, actualRootMapping ,sources)
  }


  /**
   * Creates the RootNode. the RootNode always has to be a SimpleNode, think XML Document
   * So we will wrap whatever is in the config in a SimpleNode
   * @param outputElem
   * @return
   */
  private def createTreeFromXml(outputElem: w3cd.Node): Tree[OutputNode] =

    // three scenarios when we look under the ete:output/ XML node
    // 1. one element, is not xmlns:ete -> that is the name of the Node (the root node)
    // 2. is xmlns:ete -> wrap it in a SimpleNode
    // 3. more than one element of any type or something else -> wrap it in a Simplenode

    Option(outputElem.getChildNodes.item(0)) match {
      case Some(c : org.w3c.dom.Element) if !isEteNs(c) => createTree(Some(c.getLocalName),createSubForest(c.getChildNodes))
      case Some(c : w3cd.Node) => createTree(Some(DEFAULT_OUTPUT_NODE),createSubForest(c.getChildNodes))
      case None => throw new UnsupportedEteConfigError("Missing XML for the mapping /ete:config/ete:output")
    }

  /**
   * This will generate a Sub Forrest of ete:config nodes for all Elements
   * @param childElements
   * @return
   */
  private def createSubForest(childElements: w3cd.NodeList): Stream[Tree[OutputNode]] = {
    (0 to childElements.getLength).map( i =>
      childElements.item(i) match {
        case t: w3cd.Text if !isWhitespace(t) => aio(TextNode(t.getNodeValue.trim)).leaf
        case ete: w3cd.Element if isEteNs(ete) => createEteMappingNode(ete)
        case e: w3cd.Element => createTree(Some(e.getLocalName),createSubForest(e.getChildNodes))
      }
    ).toStream
  }

  def isEteNs(n: w3cd.Node) = xpath.evaluate("namespace-uri(.)",n).equals(ETE_NS)

  def isWhitespace(n : w3cd.Node) : Boolean = n match {
      case t: w3cd.Text => t.isElementContentWhitespace
      case _ => false
    }

  /**
   * The node is of ete: namespace
   * Heere
   * @param element
   * @return
   */
  private def createEteMappingNode(element: w3cd.Element) : Tree[OutputNode] = {
    logger.info("Reading config : " + element.getTagName)
    element.getLocalName match {
      case "per-row" => createSubForestPerRow(element)
      case t: String => throw UnsupportedEteConfigError("[" + t + "] is not a supported ete config element.")
    }
  }

  private def createSubForestPerRow(element: w3cd.Element) : Tree[OutputNode]  =

    Tree.node(aio(
                       DataSetAttachedNode(element.getAttribute("name")
                      ,element.getAttribute("source")
                      ,primaryKeysFinder(element))
              )
             ,createSubForest(element.getChildNodes))


  /**
   * Each per-row node can have 0 or more keys defined.
   * @param element
   * @return
   */
  private def primaryKeysFinder(element: w3cd.Element):Vector[String] = {
     val keys = xpath.evaluate("@*[starts-with(name(), 'key')]",element,XPathConstants.NODESET).asInstanceOf[w3cd.NodeList]
     keys.getLength match {

        // if no "key" attribute was found on the node, then we will make up one as being "{name}_id"
        //case 0 => Vector(element.getTagName + "_id")

        case 0 => Vector.empty   // trying a fancier route of "automagic deriving from the column names"
        case _ => (0 to keys.getLength).map(
                      idx => keys.item(idx).asInstanceOf[w3cd.Attr].getValue
                  ).toVector

      }
  }


  /*
   * Our NameSpace Context Handler
   */
  val NsContext = new NamespaceContext {
    def getNamespaceURI(prefix: String) = prefix match {
      case ETE_NS_PREFIX => ETE_NS
      case _ => XMLConstants.NULL_NS_URI
    }
    def getPrefix(namespaceURI: String) = namespaceURI match {
      case ETE_NS => ETE_NS_PREFIX
      case _ => null
    }
    def getPrefixes(namespaceURI: String) = null

  }

  /*
   * Our XPath Handler
   */
  def xpath = {
    val xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath()
    xpath.setNamespaceContext(NsContext)
    xpath
  }

  def loadAsDocument(inputSource: InputSource):w3cd.Document = {
    val factory = DocumentBuilderFactory.newInstance()
    factory.setNamespaceAware(true)
    factory.setIgnoringElementContentWhitespace(true)
    val builder = factory.newDocumentBuilder()
    return builder.parse(inputSource)
  }

}


case class UnsupportedEteConfigError(error: String) extends EteGeneralException(error)
