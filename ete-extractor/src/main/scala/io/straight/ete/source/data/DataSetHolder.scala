package io.straight.ete.source.data

import scala.concurrent.stm.atomic

/**
 * @author rbuckland
 */
trait DataSetHolder {

  def internalCreateNewDatSet(nameMapping: DataRowNameMapping): DataSet

  /**
   * Creates a new Ecache DataSet
   * @param nameMapping
   * @param dataSetId
   * @return
   */
  def newDataSet(nameMapping: DataRowNameMapping, dataSetId: String): DataSet = {
      val dataSet:DataSet = internalCreateNewDatSet(nameMapping)
      addDataSet(dataSetId,dataSet,None)
      return dataSet
  }

  var dataSets : Map[String,DataSet] = Map.empty[String,DataSet]

  private def addDataSet(baseDataSetName: String, ds: DataSet ,newIndex:Option[DataSetIndex]) = {
    atomic { implicit txn =>
      val name = newName(baseDataSetName)
      ds.dataSetId = name
      dataSets += (name -> ds)
    }
  }

  /**
   * The names of the dataSets are "name-0, name-1, name-2, name-3" etc.
   *
   * We want the "next name to use" which is just find the "next one" and add 1
   *
   * @param name
   * @return
   */
  def newName(name: String): String = {
    val f = dataSets.keySet.filter(_.startsWith(name))
    if (f.size == 0 ) {
      return name + "-0"
    } else {
      val number = f.map(_.reverse.split("-")(0).toInt).max + 1
      return name + "-" + number
    }
  }

}


