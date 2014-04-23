package io.straight.ete.source.extract.jdbc

import io.straight.ete.source.data.{DataSet, DataRow, DataSetHolder}
import io.straight.ete.config.{JdbcSourceData, XlsSourceData}
import io.straight.ete.source.data.DataRow
import io.straight.ete.source.data
import java.sql.{Statement, ResultSetMetaData, ResultSet}
import org.slf4j.LoggerFactory

/**
 * @author rbuckland
 */
object JdbcSourceExtractor {

  val logger = LoggerFactory.getLogger(getClass)

  /**
   * @param jdbcSourceData
   * @param dataSetHolder
   * @param indexingFunction function that will take the datarow and the it, and index it.
   *
   */
  def extractJdbcData(jdbcSourceData:JdbcSourceData,
                      dataSetHolder: DataSetHolder,
                      indexingFunction: (Int,DataRow,DataSet) => DataRow
                     ):String = {

    // step 1, execute the SQL.
    val conn = jdbcSourceData.datasource.getConnection

    jdbcSourceData.sqlStatements.foreach { strStmt =>

      val preppedStatement = JdbcTools.prepSqlStatement(strStmt)
      val stmt = conn.createStatement()
      stmt.execute(preppedStatement)

      // nextResult returns an Either[ResultSet,Int]  .. rs or UpdateCount
      StatementIterator(stmt).foreach {
          // an updatecount
          case Right(updateCount) => logger.trace("ignoring the update count (" + updateCount + ")")
          // a resultSet
          case Left(resultSet) => {
            val rsIter = RsIterator(resultSet)
            val columnHeaders = collectColumnHeaders(resultSet.getMetaData)
            val colSize = columnHeaders.size

            logger.debug("ResultSet Headers " + columnHeaders)

            // create a new dataSet
            val dataSet = dataSetHolder.newDataSet(columnHeaders,jdbcSourceData.dataSetId)

            logger.debug(dataSet.dataRowIdxToColumnNames + "")

            // read the resultSet
            for (rs <- rsIter) {
              val rowData = (1 until colSize).map(c => rs.getObject(c)).toVector
              dataSet.addDataRow(indexingFunction(rs.getRow - 1, DataRow(rowData), dataSet))
              logger.trace("Extracted row: " + rowData)
            }
            logger.info("Read " + dataSet.size + " rows(s) to " + dataSet.dataSetId + " from " + jdbcSourceData)
          }
      }
    }
    logger.info("Added " + dataSetHolder.dataSets.size + " dataSet(s) from " + jdbcSourceData)
    return "Complete"
  }


  /**
   *
   * @param rsmd ResultSetMetaData
   * @return data.DataRowNameMapping column names and their indexed position
   */
  def collectColumnHeaders(rsmd: ResultSetMetaData): data.DataRowNameMapping = {
    (
      for (
        i <- 1 to rsmd.getColumnCount;
        name = rsmd.getColumnName(i)
      ) yield ( name -> (i-1) )
    ).toMap[String,Int]
  }

}

case class RsIterator(val rs: ResultSet) extends Iterator[ResultSet] {
  def hasNext: Boolean = rs.next()
  def next(): ResultSet = rs
}


case class StatementIterator(val statement: Statement) extends Iterator[Either[ResultSet, Int]] {

  private var result: Option[Either[ResultSet, Int]] = None
  private var haveCollectedFirstRs: Boolean = false

  private def nextResult(): Option[Either[ResultSet, Int]] = {

    if (haveCollectedFirstRs) {
      statement.getMoreResults
    } else {
      haveCollectedFirstRs = true
    }

    (statement.getResultSet,statement.getUpdateCount) match {
      case (null,-1) => { statement.close(); None }
      case (null,updateCount) => Some(Right(updateCount))
      case (resultSet,_) => Some(Left(resultSet))
    }
  }

  def hasNext = {
    result = nextResult()
    result.isDefined
  }
  def next() = result.get
}

/**
 * We oh so concisely wrap the statement execution inside object that we can use in a Stream
 *
 * Stream.continually(statementWrapper.nextResult()).takeWhile(_.isDefined).map {
     result => result.get match {

       // an updatecount
       case Right(updateCount) => ...

       // a resultSet
       case Left(resultSet) => ...
   }
 *
 * @param statement
 */
case class WrappedStatement(statement: Statement) {

  private var haveCollectedFirstRs: Boolean = false

  /**
   * The state of the first execution will retrun a resultset or the count
   * (as if "getMoreResults" was called.. ) stupid API
   * So .. we call getMoreResults for where resultSet beyond first
   */
  def nextResult(): Option[Either[ResultSet, Int]] = {

    if (haveCollectedFirstRs) {
      statement.getMoreResults
    } else {
      haveCollectedFirstRs = true
    }

    (statement.getResultSet,statement.getUpdateCount) match {
      case (null,-1) => { statement.close(); None }
      case (null,updateCount) => Some(Right(updateCount))
      case (resultSet,_) => Some(Left(resultSet))
    }
  }

}


class StatementIterator2(val statement: Statement) extends Iterator[Either[ResultSet, Int]] {

  private var haveCollectedFirstRs: Boolean = false
  private var resultSet: ResultSet = null
  private var updateCount:Int = -2

  def hasNext:Boolean = {
    updateCount = statement.getUpdateCount()

    // this will move us on to the next
    return if (haveCollectedFirstRs) {
      val hasMore = (statement.getMoreResults() == false) && (updateCount == -1)
      resultSet = statement.getResultSet
      hasMore
    } else {
      haveCollectedFirstRs = true
      resultSet = statement.getResultSet
      (resultSet != null) || updateCount != -1
    }

  }

  def next() = {
    (resultSet,updateCount) match {
      case (null,updateCount) => Right(updateCount)
      case (resultSet,_) => Left(resultSet)
    }
  }
}
