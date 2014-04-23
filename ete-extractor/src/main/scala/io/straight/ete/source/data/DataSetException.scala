package io.straight.ete.source.data

/**
 * @author rbuckland
 */
case class DataSetException(error: String) extends RuntimeException(error)