package io.straight.ete.config

import java.io.{OutputStream, Writer}
import io.straight.ete.source.data.{DataSet, DataRow}
import org.jvnet.inflector.RuleBasedPluralizer
import javax.xml.stream.{XMLEventWriter, XMLOutputFactory, XMLStreamWriter}
import de.odysseus.staxon.json.{JsonXMLConfig, JsonXMLConfigBuilder, JsonXMLStreamConstants, JsonXMLOutputFactory}
import javax.xml.transform.{OutputKeys, Result}
import com.ctc.wstx.stax.WstxOutputFactory
import de.odysseus.staxon.json.stream.impl.JsonStreamFactoryImpl
import de.odysseus.staxon.json.stream.JsonStreamFactory
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter


/**
 * Mapping out the data is a fairly complex task because you need to walk the tree
 * and then you also need to iterate out one row per child and then do all children
 *
 * There are two styles to do this.. the Oo way .. where each node type is responsible for
 * managing the interation out of data.. complex and hard to understand
 *
 * The other way is to make a super class that knows how to do it all (not so much functional)
 * just not so "Oo" in the sense of encapsulating the "output" handling into a 'knowing' class
 *
 * I have gone with the "super class" that knows how to do it all.. it's tricky to follow
 * but I hope that by having it all in front of your eyes you can kind of see what is going on a lttle
 * better
 *
 * The other thing to note is that outputing JSON is different to outputting XML
 *
 * XML does not need as many events for outputting as JSON Does (We need to tell JSON when we are still in a list
 * so it can add a comma (or not add a comma)
 *
 * We need to tell JSON If the element is a collection  [   ] or if it is just a node
 *
 */

object OutputStyle {
  private val pluralizer = new RuleBasedPluralizer()
  def pluralize(word: String) = pluralizer.pluralize(word)
}

/**
 * For every data format as output that we support (json, XML, other) we will have an OutputStyle
 * class.
 */
case class OutputStyle(writer: XMLStreamWriter)  {
  def writeBegin() = {
    writer.writeStartDocument()
  }
  def writeEnd() = writer.writeEndDocument()
  def writeNodeStart(outputNode: OutputNode) = {
    outputNode match {
      case x:TextNode => writer.writeCharacters(x.text)
      case x:DataSetAttachedNode => writer.writeStartElement(OutputStyle.pluralize(x.name))
      case x:ContainerNode => writer.writeStartElement(x.name)
    }
  }
  def writeText(textNode: TextNode) = writer.writeCharacters(textNode.text)
  def writeNodeEnd(outputNode: OutputNode) = {
    outputNode match {
      case x: TextNode =>
      case _ => writer.writeEndElement()
    }
  }
  def writePreRowStart(dsNode: DataSetAttachedNode) = {
    writer.writeProcessingInstruction(JsonXMLStreamConstants.MULTIPLE_PI_TARGET, dsNode.name)
    writer.writeStartElement(dsNode.name)
  }
  def writePostRowEnd(dsNode: DataSetAttachedNode) = writer.writeEndElement()

  // At first glance (lots of glancing) you will realise we need a dataRow (this is what we write out)
  // but we pass in the dataSet as well for a valid reason.
  // the dataRow is really a vector of values (no column names)
  // we also use ehcache to store these to disk if they get too big
  // if we added the col names to each row that would be mass duplication
  // so instead, when we need to resolve the names of each column, we pass along the dataSet
  // .. originally the dataRow had a pointer to the dataSet .. but the dataSet holds a ref
  // to the cache which was not serializable .. so .. it's better to just keep them separate
  def writeRowStart(dsNode:DataSetAttachedNode, dataSet: DataSet, dataRow: DataRow) = {
    dataRow.row.zipWithIndex.foreach { case(value,i) =>
      writer.writeStartElement(dataSet.dataRowIdxToColumnNames.get(i).get)
      writer.writeCharacters(value.toString)
      writer.writeEndElement()
    }
  }
  def writeRowEnd(dsNode:DataSetAttachedNode, dataSet: DataSet,dataRow: DataRow) = { }

}

object JsonOutputStyle {
  def create(out: OutputStream, rootName:String, prettyPrint: Boolean = false) : OutputStyle = {

    val factory = prettyPrint match {
      case false => new JsonXMLOutputFactory()
      case true => { // src: https://github.com/beckchr/staxon/issues/5
        val config = new JsonXMLConfigBuilder().prettyPrint(true).build()
        val streamFactory = new JsonStreamFactoryImpl(" ", "  ", "\n")
        new JsonXMLOutputFactory(config, streamFactory)
      }
    }

    //factory.setProperty(JsonXMLOutputFactory.PROP_VIRTUAL_ROOT, rootName)
    // factory.setProperty(JsonXMLOutputFactory.PROP_PRETTY_PRINT, prettyPrint) <-- didn't seem to work see above
    factory.setProperty(JsonXMLOutputFactory.PROP_MULTIPLE_PI, true)

    val xmlStreamWriter = factory.createXMLStreamWriter(out)
    OutputStyle(xmlStreamWriter)
  }
}

object XmlOutputStyle {
  def create(out: OutputStream, prettyPrint: Boolean = false) : OutputStyle = {
    // val factory = org.codehaus.stax2.XMLOutputFactory2()
    val factory =  new WstxOutputFactory()
    val xmlStreamWriter = prettyPrint match {
      case true => new IndentingXMLStreamWriter(factory.createXMLStreamWriter(out))
      case false => factory.createXMLStreamWriter(out)
    }
    OutputStyle(xmlStreamWriter)
  }
}
