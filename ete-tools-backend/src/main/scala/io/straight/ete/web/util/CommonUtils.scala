package io.straight.ete.web.util

import java.io._
import java.util.zip.{ZipEntry, ZipOutputStream}

/**
 * @author rbuckland
 */
object ZipFileUtil {

  def zipDirectory(dir: File, zipFile: File) =  {
    val fout = new FileOutputStream(zipFile)
    val zout = new ZipOutputStream(fout)
    zipSubDirectory("", dir, zout)
    zout.close()
  }

  /** Copies all bytes from the 'input' stream to the 'output' strem. */
  private def transfer(input: InputStream, out: OutputStream)
  {
    val buffer = new Array[Byte](8192)
    def transfer()
    {
      val read = input.read(buffer)
      if(read >= 0)
      {
        out.write(buffer, 0, read)
        transfer()
      }
    }
    transfer()
  }

  private def zipSubDirectory(basePath: String, dir: File, zout: ZipOutputStream):Unit = {
    val buffer = new Array[Byte](4096)
    val files = dir.listFiles()
    files.foreach { file =>

      if (file.isDirectory()) {

        val path = basePath + file.getName + "/"
        zout.putNextEntry(new ZipEntry(path))
        zipSubDirectory(path, file, zout)
        zout.closeEntry()

      } else {
        val fin = new FileInputStream(file)
        zout.putNextEntry(new ZipEntry(basePath + file.getName))
        transfer(fin,zout)
        zout.closeEntry()
        fin.close()
      }

    }
  }
}
