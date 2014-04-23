package io.straight.ete.source.data

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

/**
 * @author rbuckland
 */
@RunWith(classOf[JUnitRunner])
class SimpleDataRowSpec extends FlatSpec with ShouldMatchers {

  val columnNameMap = Map[Int,String](0 -> "person_id",1 -> "pet_name",2 -> "pet_breed")
  val dataRow1 = new DataRow(row = Vector(1, "Ramon Buckland", "Red"))


  "a data row foreach" should "produce a tuple of sets" in {
    // dataRow1 foreach  { case (name,value) => println(name + "->" + value) }
  }

}