package io.straight.ete.source.extract.csv

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

/**
 * @author rbuckland
 */
class CsvHelperSpec extends FlatSpec with ShouldMatchers {

  val csvData = """"header","header1","foobar"\n"data","data","data""""
  "the helper" should "find the first row in the csv" in {

    val headerData = CsvHelper.deriveHeaders(csvData)
    headerData.keySet should contain("header1")

  }
}
