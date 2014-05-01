package io.straight.ete.source.extract.jdbc

import io.straight.ete.config.{JndiJdbcSourceData, SimpleUserPassJdbcSourceData, SimpleJdbcSourceData, JdbcSourceData}
import java.nio.file.{Paths, Files}
import io.straight.ete.source.extract.{SourceHelper, SourceValidationException}
import javax.sql.DataSource
import org.slf4j.LoggerFactory
import java.sql.{Connection, SQLException}

/**
 * This Enricher will take the string parameters found on the source objects
 * and try and make a database connection. If it succeeds a new DataSource connection is placed on the
 * SourceData object.
 *
 * @author rbuckland
 */
object JdbcSourceHelper extends SourceHelper[JdbcSourceData] {

  val logger = LoggerFactory.getLogger(JdbcSourceHelper.getClass)

    def enrich(j: JdbcSourceData): JdbcSourceData = {

      try {
        j match {
          // the datasource may already have been set (think Spring Bean)
          case jdbc : JdbcSourceData if jdbc.datasource.isDefined => jdbc

          // no username / password
          case simple: SimpleJdbcSourceData => simple.copy(datasource = testConnect(simple.jdbcUrl, JdbcTools.dataSourceFor(simple.jdbcDriver,simple.jdbcUrl)))

          // username and password
          case simpleUP: SimpleUserPassJdbcSourceData =>
            simpleUP.copy(datasource = testConnect(simpleUP.jdbcUrl, JdbcTools.dataSourceFor(simpleUP.jdbcDriver,simpleUP.jdbcUrl, simpleUP.username, simpleUP.password)))

          // jndi
          case jndiSrc: JndiJdbcSourceData =>
            jndiSrc.copy(datasource = testConnect(jndiSrc.jndiUrl, JdbcTools.dataSourceFor(jndiSrc.jndiUrl)))
        }
      } catch {
        case e: SQLException => throw new SourceValidationException(j.toString,"SQLException: " + e.getLocalizedMessage,e)
        case e: Throwable => throw new SourceValidationException(j.toString,"Unexpected Exception: " + e.getLocalizedMessage,e)
      }
    }

  /**
   * Make a connection and close it. If it all works .. good
   */
  def testConnect(name: String, ds: DataSource) : Option[(String,DataSource)] = {
    // typical JDBC Connection protection connected malarkey
    var conn: Connection = null
    try {
      val conn = ds.getConnection
      if (logger.isDebugEnabled) {
        logger.debug("OK Connection found to " + conn.getMetaData.getURL.toString)
      }
      conn.close()
    } catch {
      case e: Throwable  => throw e
    } finally {
      if (conn != null) conn.close
    }
    Some(name -> ds)
  }

  /**
   * Called on the cleanup regardless (finally) of the state of the execution
   * @param source
   */
  def cleanup(source: JdbcSourceData): Unit = {
    // anything to do here ? not really (the datasources were ds's)
  }
}
