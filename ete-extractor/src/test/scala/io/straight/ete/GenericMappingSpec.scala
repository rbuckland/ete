package io.straight.ete

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import io.straight.ete.config.{SimpleNode, EteConfig}
import java.io.{ByteArrayOutputStream, StringWriter}
import io.straight.ete.source.extract.EteExecutor
import org.scalatest.matchers.ShouldMatchers
import io.straight.ete.config.EteConfigBuildSupport._
import scalaz.Tree

/**
 * @author rbuckland
 */
@RunWith(classOf[JUnitRunner])
class GenericMappingSpec extends FlatSpec with ShouldMatchers {

  val child = Tree.leaf(aio(SimpleNode("childNode")))

  val config = new EteConfig(
           name= "foobarConfig",
           rootMapping = Tree.node(aio(SimpleNode("parentNode")),Stream(child)),
           sources = Vector.empty)

  "mapping to lists of objects with a key of id" should "generate the correct Json" in {

    Vector("json", "xml").foreach(t => {
          val out = new ByteArrayOutputStream()
          EteExecutor.generate(config, out, Vector.empty, t, true)
          out.flush()
          println(new String(out.toByteArray, "UTF-8"))
        }
    )

  }

}