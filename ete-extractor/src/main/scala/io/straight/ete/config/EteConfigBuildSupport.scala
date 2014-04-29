package io.straight.ete.config

import scalaz.{Show, Tree}
import scala.Tuple2

/**
 * @author rbuckland
 */
object EteConfigBuildSupport {


  def treeToString(tree: Tree[OutputNode]): String = {
    implicit val show = Show.showA[io.straight.ete.config.OutputNode]
    tree.drawTree
  }


  /*
   * Less Boiler plate .asInstanceOf[]
   */
  def aio(outputNode: OutputNode) = outputNode

  def createBasicDefaultTree(rootNodeName: Option[String] = None, dataSetsToPKs: List[Tuple2[String, Vector[String]]]) =
    createTree(rootNodeName, createSubForestOfDataSetAttachedNodes(dataSetsToPKs, Vector.empty))

  def createTree(rootNodeName: Option[String], subTreeCreator: => Stream[Tree[OutputNode]]): Tree[OutputNode] = {
    println(s"Create Tree called $rootNodeName")
    Tree.node(aio(SimpleNode(rootNodeName.getOrElse(EteConfig.DEFAULT_OUTPUT_NODE))), subTreeCreator)
  }


  /**
   * Most basic, Create a Tree -- all of DataSetAttachedNodes
   *
   * @param sourcesList a list of dataSetId:String -> Vector[columnNames:String]
   * @return
   */
  def createSubForestOfDataSetAttachedNodes(sourcesList: List[Tuple2[String, Vector[String]]], parentKeys: Vector[String]): Stream[Tree[OutputNode]] = {
    sourcesList match {
      case Nil => Stream.empty
      case hd :: tl =>
        Stream(Tree.node(
          aio(DataSetAttachedNode(name = hd._1, primaryKeys = hd._2, dataSetId = hd._1 + "-0")),
          createSubForestOfDataSetAttachedNodes(tl, hd._2)
        ))
    }

  }

}
