package io.straight.ete.source.extract

import io.straight.ete.core.EteGeneralException

/**
 * @author rbuckland
 */
case class SourceValidationException(source:String,error:String, e:Throwable)
  extends RuntimeException("[" + source + "]" + error ,e)
