package io.straight.ete.source.extract.jdbc

import javax.sql.DataSource
import org.apache.commons.dbcp2.BasicDataSource
import org.jasypt.util.text.StrongTextEncryptor
import javax.naming.InitialContext
import io.straight.ete.config.SqlStatement

/**
 * @author rbuckland
 */
object JdbcTools {

  /**
   * Datasource with just the driver and the String
   * @param jdbcDriver
   * @param jdbcString
   * @return
   */
  def dataSourceFor(jdbcDriver: String, jdbcString: String): BasicDataSource = {
      val ds = new BasicDataSource
      ds.setDriverClassName(jdbcDriver)
      ds.setMaxTotal(20)
      ds.setMaxIdle(5)
      ds.setInitialSize(5)
      ds.setValidationQuery("SELECT 1")
      ds.setUrl(jdbcString)
      ds
  }

  /**
   * Datasource with the driver, url, username and password
   *
   * @param jdbcDriver
   * @param jdbcString
   * @param username
   * @param passwordEncrypted
   * @return
   */
  def dataSourceFor(jdbcDriver: String, jdbcString:String, username: String, passwordEncrypted: Option[String]): DataSource = {
    // see http://www.jasypt.org/ for more details
    val textEncryptor = new StrongTextEncryptor()
    val ds = dataSourceFor(jdbcDriver,jdbcString)
    ds.setUsername(username)
    passwordEncrypted match {
      case Some(passwordEnc) => ds.setPassword(textEncryptor.decrypt(passwordEnc))
      case None => // set nothing
    }
    ds
  }

  /**
   * Datasource from an App Server
   * @param jndiUrl
   * @return
   */
  def dataSourceFor(jndiUrl: String) = new InitialContext().lookup(jndiUrl).asInstanceOf[DataSource]

  def prepSqlStatement(sqlStmt: SqlStatement): String = sqlStmt.sqlString

}
