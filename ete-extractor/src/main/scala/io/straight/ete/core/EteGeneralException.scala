package io.straight.ete.core

abstract class EteGeneralException(error:String) extends RuntimeException(error:String) {
  val serialVersionUID:Long = 1L;
}