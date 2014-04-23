package io.straight.ete.source.data

/**
 * @author rbuckland
 */
class EhcacheDataSetHolder extends DataSetHolder {
  def internalCreateNewDatSet(nameMapping: DataRowNameMapping): DataSet = new EhcacheDataSet(nameMapping)

  // could not be bothered to type parameterise the world for this simple clean up
  def dispose = dataSets.values.foreach(_.asInstanceOf[EhcacheDataSet].dispose)
}
