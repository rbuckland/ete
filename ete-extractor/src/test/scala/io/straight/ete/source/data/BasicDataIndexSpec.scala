package io.straight.ete.source.data

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers


/**
 * @author rbuckland
 */
class BasicDataIndexSpec extends FlatSpec with ShouldMatchers {

  "The Indexing Magic" should "return the right rows" in {
    val colMapping = Map( "id" -> 0, "name" -> 1, "shoesize" -> 2)
    val ds = new InMemoryDataSet(colMapping)

    ds.addAllDataRows( Vector(
      DataRow(Vector(1,"Rob",33)),
      DataRow(Vector(2,"Amy",33)),
      DataRow(Vector(8,"Nik",21)),
      DataRow(Vector(3,"Mik",8)),
      DataRow(Vector(4,"Alf",8)),
      DataRow(Vector(6,"Jim",3)),
      DataRow(Vector(5,"Kim",33)),
      DataRow(Vector(7,"Jim",88)),
      DataRow(Vector(9,"Sam",2))
    )
    )

    //val dataSetsHolder = new InMemoryDataSet()

    // an index is a Vector tuples -> mapping to a set of ROW numbers in the DataSet
    val index = Map[Int,Vector[Int]](
      (Vector(("shoesize",33)) ## ) -> Vector(0,1,6),
      (Vector(("name","Jim")) ## ) -> Vector(5,7),
      (Vector(("name","Alf"),("shoesize",8)) ## ) -> Vector(4),
      (Vector(("id",2)) ## ) -> Vector(1)
    )

  }

}