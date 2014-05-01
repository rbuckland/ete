package io.straight.ete


import io.straight.ete.config._
import java.io.{ByteArrayOutputStream, ByteArrayInputStream, StringWriter, File}
import io.straight.ete.source.extract.xls.XlsInspector
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import io.straight.ete.source.extract.EteExecutor
import io.straight.ete.util.EteHelper

/**
 * @author rbuckland
 */
class AutoInspectMappingSpec extends FlatSpec with ShouldMatchers {


  val sample = new File(Thread.currentThread().getContextClassLoader.getResource("1sheet-sample.xls").getFile)

  "Generating a default config from an XLS" should "just work" in {

    val config = XlsInspector.createConfig(sample, sample.getAbsoluteFile.toString)

    println(EteConfigBuildSupport.treeToString(config.rootMapping))

    Vector("json","xml").foreach( t =>
    {
      val out = new ByteArrayOutputStream()
      EteExecutor.generate(config,out,Vector.empty,t, true)
      out.flush()
      println(new String(out.toByteArray,"UTF-8"))
    }
    )
  }

}