package io.straight.ete.source.extract

import io.straight.ete.config._
import org.slf4j.LoggerFactory
import java.io.{OutputStream, Writer}
import io.straight.ete.source.data.{DataSetException, EhcacheDataSetHolder, DataSetHolder}
import io.straight.ete.source.extract.xls.{XlsSourceHelper, XlsSourceExtractor}
import io.straight.ete.config.Parameter
import io.straight.ete.config.XmlOutputStyle
import io.straight.ete.config.JsonOutputStyle
import io.straight.ete.config.XlsSourceData
import io.straight.ete.source.extract.jdbc.{JdbcSourceHelper, JdbcSourceExtractor}
import io.straight.ete.source.data.indexing.IndexMaster
import java.sql.SQLException
import io.straight.ete.core.EteGeneralException
import io.straight.ete.worker.EteDataSetMapper
import scalaz.Show

/**
 * @author rbuckland
 */
object EteExecutor {

  val logger = LoggerFactory.getLogger(EteExecutor.getClass)

  def generate(config: EteConfig, outStream: OutputStream, parameters: Vector[Parameter], outputStyle: String) = {
    try {
      /*
       * Enrich all the Sources
       */
      val enrichedConfig = config.copy(sources = doEnrich(config.sources))
      internalGenerate(enrichedConfig,outStream,parameters,outputStyle)
      doClose(config.sources)
    } catch {
      case e: SourceValidationException => throw e
      case e: SQLException => throw e
      case e: DataSetException => throw e
      case e: EteGeneralException => {
        logger.warn("Unexpected EteGeneralException occured. TODO is to handle better. " + e.getClass)
        throw e
      }
      case e: Throwable => {
        logger.warn("Unexpected internal exception during extraction [" + e.getLocalizedMessage + "]. TODO is to handle better. " + e.getClass)
        throw e
      }
    }
  }


  def internalGenerate(config: EteConfig, outStream: OutputStream, parameters: Vector[Parameter], outputStyle: String) = {

    val outputStyleHandler = outputStyle match {
      case "json" => JsonOutputStyle.create(outStream,"jsonData")
      case "xml" => XmlOutputStyle.create(outStream)
    }

    val dataSetHolder = new EhcacheDataSetHolder()

    try {

      // extract all the data
      for (
          result <- config.sources.map {
            case x : XlsSourceData => XlsSourceExtractor.extractXlsData(config,x,dataSetHolder,IndexMaster.createIndexFunction(config.rootMapping))
            case j : JdbcSourceData => JdbcSourceExtractor.extractJdbcData(j,dataSetHolder,IndexMaster.createIndexFunction(config.rootMapping))
          }
      ) yield result // they all just return "complete" for now

      // if we received no dataSets during extraction that would be bad (in theory the code
      // will have improved deep in the bowels above so that they throw more specific exceptions
      // for now we will happily throw this one .. as so many things could have gone wrong and un-noticed

      if (dataSetHolder.dataSets.size == 0 ) {
        implicit val show = Show.showA[io.straight.ete.config.OutputNode]
        throw new NoExtractedDataFoundException("No Data was found during extraction. " +
          "Configured sources were " + config.sources.map{_.dataSetId} +
          ". Configured mapping was \n" + config.rootMapping.drawTree )
      }

      // start the mapping exercise
      val mapper = new EteDataSetMapper(config.rootMapping,dataSetHolder)
      println(s">>> Executing mapper.output for ${outputStyle}")
      mapper.output(outputStyleHandler)

    } finally {
      dataSetHolder.dispose // junk our ehcache data regardless of what happened
    }

  }


  def doEnrich(sources: Vector[SourceDataConfig]): Vector[SourceDataConfig] = {
    sources.map {
          case x: XlsSourceData => XlsSourceHelper.enrich(x)
          case j: JdbcSourceData => JdbcSourceHelper.enrich(j)
    }
  }

  /**
   * Perform some (any) cleanup operations specific to each source type
   * @param sources
   */
  private def doClose(sources: Vector[SourceDataConfig]): Unit = {
    sources.map {
        case x: XlsSourceData => XlsSourceHelper.cleanup(x)
        case j: JdbcSourceData => JdbcSourceHelper.cleanup(j)
    }
  }

}

case class NoExtractedDataFoundException(message: String) extends EteGeneralException(message)
