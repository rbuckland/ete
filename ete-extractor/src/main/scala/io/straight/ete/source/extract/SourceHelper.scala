package io.straight.ete.source.extract

import io.straight.ete.config.SourceDataConfig

/**
 * @author rbuckland
 */
trait SourceHelper[S <: SourceDataConfig] {

  /**
   * Called pre the execution to setup and special things it may need
   * @param source
   * @return
   */
  def enrich(source: S): S

  /**
   * Called on the cleanup regardless (finally) of the state of the execution
   * @param source
   */
  def cleanup(source: S): Unit

}
