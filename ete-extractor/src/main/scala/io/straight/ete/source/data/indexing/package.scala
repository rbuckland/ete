package io.straight.ete.source.data

import scala.collection.immutable.TreeMap
import scala.collection.mutable
import scala.Int

/**
 * @author rbuckland
 */
package object indexing {
  /*
   * An index key is simply the set of "tuple" column names and the values
   * the hash(##) is used from the IndexKey as the Index lookup.
   */
  type IndexKey = Vector[Tuple2[String,Any]]

  /**
   * The index .. key:Int is IndexKey(##)
   * A DataSet owns an Index
   */
  type Index = mutable.HashMap[Int, Set[Int]] with mutable.MultiMap[Int, Int]
}
