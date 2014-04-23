package io.straight.ete.util

import io.straight.ete.config.{DataSetAttachedNode, OutputNode}
import io.straight.ete.core.EteGeneralException

import io.straight.ete.config.EteConfigBuildSupport._
import scalaz._
import Scalaz._
import scalaz.syntax.ToMonadPlusOps

/**
 * @author rbuckland
 */
object EteHelper {

  object scalazExtensions extends ToMonadPlusOps

  // i was going to do away with vector of vector[String] and instead use Vector[Product] but that
  // has not eventuated yet - leaving this here because it looks pretty.
  // IT is not Used :-)
  def vectorToTuple(vector: Vector[Any]):Product = {
    vector match {
      case Vector(a) => Tuple1(a)
      case Vector(a,b) => (a,b)
      case Vector(a,b,c) => (a,b,c)
      case Vector(a,b,c,d) => (a,b,c,d)
      case Vector(a,b,c,d,e) => (a,b,c,d,e)
      case Vector(a,b,c,d,e,f) => (a,b,c,d,e,f)
      case Vector(a,b,c,d,e,f,g) => (a,b,c,d,e,f,g)
      case Vector(a,b,c,d,e,f,g,h) => (a,b,c,d,e,f,g,h)
      case Vector(a,b,c,d,e,f,g,h,i) => (a,b,c,d,e,f,g,h,i)
      case Vector(a,b,c,d,e,f,g,h,i,j) => (a,b,c,d,e,f,g,h,i,j)
      case Vector(a,b,c,d,e,f,g,h,i,j,k) => (a,b,c,d,e,f,g,h,i,j,k)
      case Vector(a,b,c,d,e,f,g,h,i,j,k,l) => (a,b,c,d,e,f,g,h,i,j,k,l)
      case Vector(a,b,c,d,e,f,g,h,i,j,k,l,m) => (a,b,c,d,e,f,g,h,i,j,k,l,m)
      case Vector(a,b,c,d,e,f,g,h,i,j,k,l,m,n) => (a,b,c,d,e,f,g,h,i,j,k,l,m,n)
      case Vector(a,b,c,d,e,f,g,h,i,j,k,l,m,n,o) => (a,b,c,d,e,f,g,h,i,j,k,l,m,n,o)
      case _ => throw new CompletelyUnsupportedError("We don't support more than 14 elements (is this the primary key list ?? seriously ?)")
    }
  }

  /**
   * Get all the dataSetAttachedNodes into a sequence
   * @param node
   * @return
   */
  @deprecated("not needed now","Other")
  def sequenceOfDataSetAttachedNodes(node: Tree[OutputNode]): Stream[DataSetAttachedNode] = node.flatten.map { case d: DataSetAttachedNode => d }

  /**
   * We need to walk the tree and put the primary keys on the dataSetAttachedNodes.. if they haven't got one yet.
   * This occurs if the config autogenerated
   *
   * @param tree
   * @param dataSetId
   * @param columnNames
   * @return
   */
  def applyColNamesToPrimaryKeys(tree: Tree[OutputNode], dataSetId: String, columnNames: Set[String]): Tree[OutputNode]  = {

    println("tree before: \n" + treeToString(tree))

    // we could use a TreeLoc zipper here -- but the life of me I could not find good documentation on
    // how to walk the tree for all DataSetAttachedNodes (missing implicit MonadOpPlus whan trying .filter
    val newTree = tree.map {
      case d: DataSetAttachedNode if  d.dataSetId == dataSetId && d.primaryKeys.isEmpty => d.initialisePrimaryKeysFromColumnNames(columnNames)
      case x: OutputNode => x
    }
    println("tree after: \n" + treeToString(newTree))
    newTree
  }


}

case class CompletelyUnsupportedError(error: String) extends EteGeneralException(error)
