package io.straight.ete.source.data

import scala.concurrent.stm.atomic
import org.slf4j.LoggerFactory


/**
 * @author rbuckland
 */
class InMemoryDataSetHolder extends DataSetHolder {
  def internalCreateNewDatSet(nameMapping: DataRowNameMapping): DataSet = new InMemoryDataSet(nameMapping)
}


