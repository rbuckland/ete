package io.straight.ete

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import io.straight.ete.source.data.{InMemoryDataSet, DataRow}
import org.scalatest.matchers.ShouldMatchers

/**
 * @author rbuckland
 */
class BasicMappingSpec extends FlatSpec with ShouldMatchers {


  val ds1 = new InMemoryDataSet(Map( "person_id" -> 0, "name" -> 1, "colour" -> 2))
  val ds2 = new InMemoryDataSet(Map( "person_id" -> 0, "pet_name" -> 1, "pet_breed" -> 2))

  // this represents what came from a query
  ds1.addAllDataRows( Vector(
    DataRow(Vector(1,"Ramon Buckland","Red")),
    DataRow(Vector(2,"James MacDonald","Blue")),
    DataRow(Vector(3,"Jim Juno","Yellow")),
    DataRow(Vector(4,"Harry Haam","Orange")),
    DataRow(Vector(5,"Mike Vissi","Green")),
    DataRow(Vector(6,"Sue Vin","Pink")),
    DataRow(Vector(7,"Sam Voo","Black")),
    DataRow(Vector(8,"Kylie Mikaa","Brown")),
    DataRow(Vector(9,"Kelly Karr","White"))
  ) )

  ds2.addAllDataRows( Vector(
    DataRow(Vector(1,"Bobo","Dog")),
    DataRow(Vector(1,"Fifi","Elephant")),
    DataRow(Vector(2,"Juju","Flea")),
    DataRow(Vector(3,"Voy","Cat")),
    DataRow(Vector(3,"Slippery","Snake")),
    DataRow(Vector(4,"Vex","Fly")),
    DataRow(Vector(5,"Chin-chin","Kangaroo")),
    DataRow(Vector(6,"Bruce","Pig")),
    DataRow(Vector(7,"Dorris","Horse")),
    DataRow(Vector(8,"Ba-ba","Gorilla")),
    DataRow(Vector(9,"Houdini","Rhino")),
    DataRow(Vector(9,"Big","Mouse"))
   )
  )

}