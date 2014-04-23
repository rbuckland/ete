package io.straight.ete.source.data

import net.sf.ehcache.{Element, CacheManager}
import org.slf4j.LoggerFactory
import java.util.UUID
import java.io.Serializable

/**
 * @author rbuckland
 */
case class EhcacheDataSet(dataRowColumnNames : DataRowNameMapping) extends DataSet {
  private val SizeOfElementsForDebug = 5000
  val logger = LoggerFactory.getLogger(classOf[EhcacheDataSet])
  val cacheId = UUID.randomUUID().toString
  val cache = {
    EhcacheDataSet.cacheManager.addCache(cacheId)
    EhcacheDataSet.cacheManager.getCache(cacheId)
  }
  logger.debug("New ehcache create id=[" + cacheId + "]")

  def addDataRow(dataRow: DataRow): Unit = {
      cache.put(new Element(cache.getKeys.size, dataRow.asInstanceOf[Serializable]))
      if (logger.isDebugEnabled) {
        if (cache.getSize % SizeOfElementsForDebug == 0) {
          logger.debug("Cache [" + cacheId + "] " + cache.getSize + " objects added")
        }
      }
    }

  /**
   * USed when we iterate through every row
   */
  def dataRowsIterator: Iterator[DataRow] = {
    import scala.collection.JavaConverters._
    case class DataRowIterator(keysIter:Iterator[Int]) extends Iterator[DataRow]{
      def hasNext: Boolean = keysIter.hasNext
      def next(): DataRow = {
        cache.get(keysIter.next().asInstanceOf[Serializable]).getObjectValue.asInstanceOf[DataRow]
      }
    }
    return DataRowIterator(cache.getKeys.iterator.asScala.asInstanceOf[Iterator[Int]])
  }

  def getDataRow(rowNumber: Int): DataRow = {
    cache.get(rowNumber.asInstanceOf[Serializable]).getObjectValue.asInstanceOf[DataRow]
  }

  def size: Int = cache.getKeys.size

  def dispose = {
    this.cache.dispose();
    EhcacheDataSet.cacheManager.removeCache(cacheId);
  }
}

object EhcacheDataSet {
  // we need to ensure we don't clobber someone elses ehcache implementation
  // so we use our own file
  val cacheManager = CacheManager.create(this.getClass.getClassLoader.getResourceAsStream("ehcache-ete.xml"))

}