package io.straight.ete.source.extract

import io.straight.ete.config.{EteConfig, SourceDataConfig}
import io.straight.ete.source.data.{DataSet, DataRow, DataSetHolder}

/**
 * @author rbuckland
 */
/**
 * @author rbuckland
 */
trait SourceExtractor[S <: SourceDataConfig] {
  def extractData(config: EteConfig, sourceData:S, dataSetHolder: DataSetHolder, indexingFunction: (Int,DataRow,DataSet) => DataRow) : String
}

