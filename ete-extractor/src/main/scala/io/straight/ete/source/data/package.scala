package io.straight.ete.source

import scala.Tuple2

/**
 * @author rbuckland
 */
package object data {

  type DataSetId = String

  // A map of the hash from the IndexKey ## and a vector of the RowNumbers
  type DataSetIndex = Map[Int,Vector[Int]]

  // a set of Column and Value Tuples
  type IndexKey = Vector[Tuple2[String,Any]]

  // each dataset row will need to be indexed.
  // this PrimaryKeys represents a full set of "dataSet" key's that need to be turned into "IndexKeys'
  // So the mapping is [[id],[parent_key_id]]
  //    turns into [(id,?) ## --> [rows,...] ]
  // where (id,?) is the Index Key
  // and the full [(id,?) ## --> [rows,...] ] is the DataSetIndex
  type PrimaryKeys = Vector[Vector[String]]

  /**
   * Take a list of column names and "derive" which ones should be the
   * primary keys eg. id, personId, person_id, job_id etc
   */
  type UnknownPrimaryKeyFunctor = (IndexedSeq[String]) => Vector[String]

  // a column name to "vector" index in the row
  type DataRowNameMapping = Map[String,Int]

  // [ (col1, 1) , (col3,1) ]
  // ?? NOT SURE is this just an IndexKey
  type KeySet = Vector[(String,Any)]


}
