package io.straight.ete.source.extract.xls

import io.straight.ete.config.XlsSourceData
import java.nio.file.{Paths, Path, Files}
import io.straight.ete.source.extract.{SourceHelper, SourceValidationException}

/**
 * Fancy name - but all we do is check that the file is there
 *
 * @author rbuckland
 */
object XlsSourceHelper extends SourceHelper[XlsSourceData] {

  def enrich(x: XlsSourceData): XlsSourceData = {
     if (!Files.exists(Paths.get(x.file.getAbsolutePath))) {
       throw new SourceValidationException(x.dataSetId + "/" + x.sourceName,"File [" + x.file.getAbsoluteFile + "] was not found",null)
     }
    x
  }

  /**
   * Called on the cleanup regardless (finally) of the state of the execution
   * @param source
   */
  def cleanup(source: XlsSourceData): Unit = {
   // not sure if we need to do anything .. ?maybe?
   //    (code refactor and pass in InputSources instead of File refs'
   //     and in Here close them.)
  }

}
