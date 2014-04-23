package io.straight.ete.source.extract.xls

import org.apache.poi.hssf.usermodel.{HSSFWorkbook, HSSFSheet}
import org.apache.poi.poifs.filesystem.{NPOIFSFileSystem, POIFSFileSystem}
import java.io.{File, InputStream, FileInputStream}
import io.straight.ete.source.data
import org.apache.poi.ss.usermodel.{Cell, Row}

/**
 * @author rbuckland
 */
object XlsHelper {

  /**
   * Simple helper to load a workbook
   * @param inputStream
   * @return
   */
  def loadWorkbook(inputStream: InputStream): HSSFWorkbook = {
    val fs: POIFSFileSystem = new POIFSFileSystem(inputStream)
    return new HSSFWorkbook(fs, true)
  }

  def loadWorkbook(file: File): HSSFWorkbook = {
    val poifs = new NPOIFSFileSystem(file)
    return  new HSSFWorkbook(poifs.getRoot,true)
  }

  /**
   * Helper loader to read the SS worksheet
   *
   * @return sheet HSSFSheet
   */
  def loadSheet(inputStream: InputStream, sheetName: Option[String]): HSSFSheet = {

    val workbook = loadWorkbook(inputStream)

    return sheetName match {
      case None =>  workbook.getSheetAt(0)
      case Some(name) => workbook.getSheet(name)
    }
  }

  /**
   * Overloaded  pass in a file
   *
   * @param file
   * @param sheetName
   * @return
   */
  def loadSheet(file: File, sheetName: Option[String]): HSSFSheet = {

    val workbook = loadWorkbook(file)

    return sheetName match {
      case None =>  workbook.getSheetAt(0)
      case Some(name) => workbook.getSheet(name)
    }
  }

  /**
   * Work through  the first row found in the spreadsheet and
   * extract out the column names (and their "number") to a Map
   * @param xlsSourceName Name of the source (could be file)
   * @param sheet
   * @return
   */
  def deriveHeaders(xlsSourceName: String, sheet: HSSFSheet):data.DataRowNameMapping = {
    val r: Row = sheet.getRow(sheet.getFirstRowNum)

    (for (
        cellNumber <- r.getFirstCellNum until r.getLastCellNum;
        cell = r.getCell(cellNumber, Row.RETURN_NULL_AND_BLANK);
        columnName = deriveColumnName(xlsSourceName,sheet,Some(cell), cellNumber, sheet.getFirstRowNum)
      ) yield (columnName -> (cellNumber - r.getFirstCellNum))
    ).toMap
  }



  /**
   * Given an Excel cell positioned at the first row in the datatable, derive
   * the column name.
   */
  def deriveColumnName(xlsSourceName: String, sheet: HSSFSheet,cellOption: Option[Cell], cellNumber: Int = -1, rowNumber: Int = -1): String = {

    cellOption match {
      case None => throwXlsExcep(xlsSourceName,sheet,null,
        "We overshot iterating the Sheet somehow, I can't continue with this sheet (I was looking at "
          + rowNumber + "," + cellNumber + ")")
      case Some(cell) => {
        cell.getCellType match {
          case Cell.CELL_TYPE_STRING => {
            return cell.getStringCellValue
          }
          case Cell.CELL_TYPE_FORMULA => {
            cell.getStringCellValue // hopefully the formula is giving a nice column name for us
          }
          case Cell.CELL_TYPE_BOOLEAN => throwXlsExcep(xlsSourceName,sheet,cell," We can't use a BOOLEAN value for a column name")
          case Cell.CELL_TYPE_NUMERIC => throwXlsExcep(xlsSourceName,sheet,cell," We can't use a NUMERIC value for a column name")
          case Cell.CELL_TYPE_ERROR => throwXlsExcep(xlsSourceName,sheet,cell," Cell has an error, can't use it for a column name")
          case Cell.CELL_TYPE_BLANK => throwXlsExcep(xlsSourceName,sheet,cell," We need a column name but I found a blank")
          case _ => throwXlsExcep(xlsSourceName,sheet,cell,"Unknown Cell Type")
        }
      }
    }
  }

  /**
   * Strips down the value in the Excel Cell to a basic Boolean, Number or String of some form
   *
   * @param c
   * @return
   */
  def valueFromCell(c: Cell) = {
    if (c == null)
      null
    else
      c.getCellType match {
        case Cell.CELL_TYPE_STRING =>  c.getStringCellValue
        case Cell.CELL_TYPE_BOOLEAN => c.getBooleanCellValue
        case Cell.CELL_TYPE_NUMERIC => c.getNumericCellValue
        case Cell.CELL_TYPE_ERROR => String.valueOf(c.getErrorCellValue)
        case Cell.CELL_TYPE_FORMULA => c.getCellFormula
        case Cell.CELL_TYPE_BLANK => null // we could return the empty string.. but null seems more correct

          // TODO tidy up this exception handling so we have all values relevant to throw a nice exception
        case _ => throw new EteXlsException("Unable to determine the cell value")
      }
  }

  /**
   * Helper to throw an exception giving specfic details about which cell/sheet had the issue
   * @param sheet
   * @param c
   * @param msg
   * @return
   */
  def throwXlsExcep(xlsSourceName: String, sheet: HSSFSheet, c: Cell, msg: String) =   {
    val cellReference = new org.apache.poi.ss.util.CellReference(c)
    val niceName = xlsSourceName + (Option(sheet.getSheetName) match {
      case None => ""
      case Some(sheetName) => "!" + sheetName
    })
    throw new EteXlsException("[" + niceName + "] at " + cellReference.formatAsString + " " + msg)
  }
}
