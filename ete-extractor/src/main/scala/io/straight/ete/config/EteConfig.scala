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
import org.w3c.dom.NodeList
import io.straight.ete.util.CompletelyUnsupportedError
import java.io.File

/**
 * @author rbuckland
 */
case class EteConfig(name: String, rootMapping: Tree[OutputNode], sources: Vector[SourceDataConfig])

object EteConstants {
  val ETE_NS = "http://io.straight/ete"
  val ETE_NS_PREFIX = "ete"
  val eteNs = scala.xml.NamespaceBinding("ete",ETE_NS,null)
  val DEFAULT_OUTPUT_NODE = "data"
}

object EteSourceType extends Enumeration {
  type Source = Value
  val XLS, XLSX, CSV, SQL = Source
}

object EteConfig {

  import EteConstants._
  import io.straight.ete.util.XmlUtils._

  val logger = LoggerFactory.getLogger(getClass)
  val DEFAULT_OUTPUT_NODE = "data"

  // this works in Oxygen .. but fails to collectr what I want here
  // text()[normalize-space(.) != ''] | //node()[local-name()!=''] .. reverting to Scala
  val XPathNodesAndNonEmptyText = "node()" // *|text()[normalize-space()]"


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
    val allSourceNodes = xpath.evaluate("/ete:config/ete:source",configXml,XPathConstants.NODESET).asInstanceOf[org.w3c.dom.NodeList]
    val outputNode = xpath.evaluate("/ete:config/ete:output",configXml,XPathConstants.NODE).asInstanceOf[org.w3c.dom.Node]
    println(s">> outputNode == ${outputNode.getLocalName}")
    val sources: Vector[SourceDataConfig] = createSourcesConfig(allSourceNodes)
    // todo collect the sources
    return createEteConfig(configName,createTreeFromXml(outputNode),sources)
  }

  /**
   * A Builder method for making an eteConfig
   * @param name
   * @param rootMapping
   * @param sources
   * @return
   */
  def createEteConfig(name: String, rootMapping: Tree[OutputNode], sources: Vector[SourceDataConfig]):EteConfig = {

    // we need to check and make sure the root mapping is a simple node.. it always has to be
    val actualRootMapping: Tree[OutputNode] = rootMapping match {
      case Tree.Node(SimpleNode(_),_) => rootMapping
      case _ => aio(SimpleNode(DEFAULT_OUTPUT_NODE)).node(rootMapping)
    }
    return EteConfig(name, actualRootMapping ,sources)
  }

  /**
   * Iterate through the source nodes and create relevant config
   * @param sourcesNodes
   * @return
   */
  def createSourcesConfig(sourcesNodes: org.w3c.dom.NodeList):Vector[SourceDataConfig] = {
    ((0 until sourcesNodes.getLength) map { idx =>
      val name = sourcesNodes.item(idx).getAttributes.getNamedItem("name").getNodeValue

      // if there is a rowlimit, collect as Option[String]
      val rowLimit = collectRowLimit(sourcesNodes.item(idx))

      sourcesNodes.item(idx).getAttributes.getNamedItem("type").getNodeValue.toLowerCase match {
        case "xls" => createSourcesForXls(name, rowLimit, sourcesNodes.item(idx))
        case "xlsx" => createSourcesForXls(name, rowLimit, sourcesNodes.item(idx))
        case "sql" => createSourcesForSql(name, rowLimit, sourcesNodes.item(idx))
        case "csv" =>  throw new UnsupportedEteConfigError("CSV Sources are not yet implemented")
        case unkn: Any => throw new UnsupportedEteConfigError(s"The type [$unkn] on source element /config/source[@name='$name'] is unsupported/unknown")
      }
    }).toVector.flatten // Vector[Vector[SourceDataConfig] to Vector[SourceDataConfig]
  }

  /**
   * Will generate a Vector of JdbcSourceData objects
   *
   * Supports - JndiJdbcSourceData, SimpleJdbcSourceData, SimpleUserPassJdbcSourceData
   * @param name
   * @param parentRowLimit
   * @param sourceNode
   * @return
   */
  def createSourcesForSql(name: String, parentRowLimit:Option[String], sourceNode: w3cd.Node): Vector[JdbcSourceData] = ???

  /**
   * Create a list of XlsSourceConfig
   * @param name
   * @param parentRowLimit
   * @param sourceNode
   * @return
   */
  def createSourcesForXls(name: String, parentRowLimit:Option[String], sourceNode: w3cd.Node): Vector[XlsSourceData] = {
    val sheetNodes = xpath.evaluate("ete:sheetname",sourceNode,XPathConstants.NODESET).asInstanceOf[org.w3c.dom.NodeList]
    val xlsUri = sourceNode.getAttributes.getNamedItem("uri").getNodeValue
    if (sheetNodes.getLength > 0) {
      collectXlsSourceBySheetNames(name, sheetNodes, xlsUri, parentRowLimit)
    } else {
       throw new UnsupportedEteConfigError("Sorry - we need you to define the sheetnames in the XML: Future versions may use the XlsInspector")
       /*
        Thge XLSInpsector has a method that derives the XLSSourceConfig from the XLS. However I want to
        clean that code up and it is a rabbit warren around IOStream handling .. so holding that off for the moment.
        (it stems that I want to real from http:// URLs but do I cache the result using ete-extractor or do I have (always)
        something else provide a File for ete-extractor
        context is - // http://poi.apache.org/spreadsheet/quick-guide.html#FileInputStream
        */
    }

  }

  /**
   * This processes the "sheetname" nodes inside of
   *
   * <pre>
   *   <ete:source name="products" type="xlsx" uri="file://src/test/resources/sample-products-options-spreadsheet.xlsx" rowlimit="1-20">
         <ete:sheetname rowlimit="2-4">products</ete:sheetname>
         <ete:sheetname>otherSheet</ete:sheetname>
       </ete:source>
     </pre>

   * @param name
   * @param sheetNodes
   * @param xlsUri
   * @param parentRowLimit
   * @return
   */
  private def collectXlsSourceBySheetNames(name:String,
                                           sheetNodes: w3cd.NodeList,
                                           xlsUri: String,
                                           parentRowLimit: Option[String]
                                          ):Vector[XlsSourceData] = {
      ((0 until sheetNodes.getLength) map { idx =>

        val sheetName = sheetNodes.item(idx).getNodeValue

        val rowLimit = collectRowLimit(sheetNodes.item(idx)) match {
          case None => parentRowLimit
          case rl: Some[String] => rl
        }

        XlsSourceData(dataSetId = s"$name-$idx",
          file = new File(xlsUri),
          sourceName = xlsUri,
          sheetName = Some(sheetName),
          rowRestrictor = rowLimit)

      }).toVector
  }

  private def collectRowLimit(node: w3cd.Node):Option[String] =  Option(node.getAttributes.getNamedItem("rowlimit")).map(_.getNodeValue)


  /**
   * Creates the RootNode. the RootNode always has to be a SimpleNode, think XML Document
   * So we will wrap whatever is in the config in a SimpleNode
   * @param outputElem
   * @return
   */
  private def createTreeFromXml(outputElem: w3cd.Node): Tree[OutputNode] = {

    // three scenarios when we look under the ete:output/ XML node
    // 1. one element, is not xmlns:ete -> that is the name of the Node (the root node)
    // 2. is xmlns:ete -> wrap it in a SimpleNode
    // 3. more than one element of any type or something else -> wrap it in a Simplenode
    println(s"XML Tree rootNode = ${outputElem.getChildNodes.getLength} ==  ${outputElem.getChildNodes.item(0)}")

    // the XML should have only 1 Node underneath the output node
    Option(getNode(outputElem,"*[1]")) match {
      case Some(c: org.w3c.dom.Element) if !isEteNs(c) => createTree(Some(c.getLocalName), createSubForest(getNodes(c,XPathNodesAndNonEmptyText)))
      case Some(c: w3cd.Node) => createTree(Some(DEFAULT_OUTPUT_NODE), createSubForest(getNodes(c,XPathNodesAndNonEmptyText)))
      case None => throw new UnsupportedEteConfigError("Missing XML for the mapping /ete:config/ete:output")
    }
  }

  /**
   * This will generate a Sub Forrest of ete:config nodes for all Elements
   * @param childElements
   * @return
   */
  private def createSubForest(childElements: w3cd.NodeList): Stream[Tree[OutputNode]] = {
    // this is slightly inefficient - we loop twice .. looking for empt text nodes (I tried to sort that in the xpath) but Java would not have it.
    (0 until childElements.getLength).filter ( i =>
      childElements.item(i) match {
        case t: w3cd.Text if stripAllWhitespace(t).length == 0 => false
        case _ => true
      }
     ).map { i =>
        childElements.item(i) match {
          case t: w3cd.Text => aio(TextNode(stripAllWhitespace(t))).leaf
          case ete: w3cd.Element if isEteNs(ete) => createEteMappingNode(ete)
          case e: w3cd.Element => createTree(Some(e.getLocalName), createSubForest(getNodes(e, XPathNodesAndNonEmptyText)))
        }
    }.toStream
  }

  def isEteNs(n: w3cd.Node) = xpath.evaluate("namespace-uri(.)",n).equals(ETE_NS)

  /**
   * If the node is Null ==> ""
   * If the node has whitespace strip it
   * @param n
   * @return
   */
  def stripAllWhitespace(n : w3cd.Text) : String = Option(n.getNodeValue) match {
    case Some(untrimmed)  => untrimmed.replaceAll("^[\\s]+|[\\s]+$","") // remove lead and trail whitespace
    case None => ""
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
        case _ => (0 until keys.getLength).map(
                      idx => keys.item(idx).asInstanceOf[w3cd.Attr].getValue
                  ).toVector

      }
  }


}


case class UnsupportedEteConfigError(error: String) extends EteGeneralException(error)
