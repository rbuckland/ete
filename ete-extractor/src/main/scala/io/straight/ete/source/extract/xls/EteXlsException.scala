package io.straight.ete.source.extract.xls

/**
 * Our Exception
 * @param error
 * @param serialVersionUID
 */
case class EteXlsException(error:String, serialVersionUID:Long = 1L) extends RuntimeException(error:String)
