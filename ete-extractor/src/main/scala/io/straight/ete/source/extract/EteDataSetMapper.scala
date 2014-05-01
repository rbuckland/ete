package io.straight.ete.source.extract

import io.straight.ete.source.data._


import org.slf4j.LoggerFactory

import scalaz._
import io.straight.ete.config._
import io.straight.ete.config.TextNode
import scala.Some
import io.straight.ete.source.data.DataSetException
import io.straight.ete.config.DataSetAttachedNode

class EteDataSetMapper(rootNode: Tree[OutputNode], dataSetHolder: DataSetHolder) {

  val logger = LoggerFactory.getLogger(classOf[EteDataSetMapper])

  def output(outputStyle: OutputStyle) {
    outputStyle.writeBegin()
    outputInternal(rootNode,outputStyle, None, true)
    outputStyle.writeEnd()
  }

  /**
   * This little number is the magic output code.
   * All the hard work before us becomes a lot easier in here
   * @param node
   * @param outputStyle
   * @param keys
   */
  private def outputInternal(node: Tree[OutputNode],outputStyle:OutputStyle, keys:Option[IndexKey], isLastSibling: Boolean, depth: Int =0) {

    // we need to trigger to the outputhandler a lot of calls, 9
    node.rootLabel match {

      case x:TextNode => outputStyle.writeText(x)

      case c:SimpleNode => {
        outputStyle.writeNodeStart(c)
        node.subForest.foreach (
          child => {
            val x = child.loc
            outputInternal(child,outputStyle,keys, x.isLast, depth + 2)
          }
        )
        outputStyle.writeNodeEnd(c)
      }


      case dsNode:DataSetAttachedNode => {

        // start our output
        outputStyle.writeNodeStart(dsNode)

        // find out associated dataSet
        val dataSet = dataSetHolder.dataSets.get(dsNode.dataSetId) match {
          case None => throw new DataSetException("Boom! Cannot find dataSet[" + dsNode.dataSetId + "] - Known are [" + dataSetHolder.dataSets.keySet + "]")
          case Some(ds) => ds
        }

        // get the rows from the dataSet and iterate
        val rows = dataSet.dataRowIterator(keys)
        rows foreach { row =>

          outputStyle.writePreRowStart(dsNode)
          outputStyle.writeRowStart(dsNode,dataSet,row)
          outputStyle.writeRowEnd(dsNode,dataSet,row)

          val myKeys = dsNode.indexKeyFromRow(row,dataSet.dataRowColumnNames)
          node.subForest.foreach (
            child => {
              val x = child.loc
              outputInternal(child, outputStyle, Some(myKeys), x.isLast)
            }
          )
          outputStyle.writePostRowEnd(dsNode) // XML end row Node
        }

        outputStyle.writeNodeEnd(dsNode)
      }

    }
  }
}





