package io.straight.ete.web.model

import io.straight.fw.model._
import java.nio.file.{Paths, Files}
import scalaz._
import Scalaz._
import org.joda.time.DateTime
import io.straight.ete.web.messages._
import io.straight.ete.web.messages.AddNewFile
import io.straight.ete.web.messages.NewFileAdded

/**
 * @author rbuckland
 */
case class StoredFile(
                       override val id: Uuid,
                       override val version: Long,
                       displayName: String,
                       filename: String,
                       filesize: Int,
                       createdDateTime:DateTime,
                       changedDateTime:Option[DateTime] = None)
  extends UuidBaseDomain(id,version) {

  def fileExists() = Files.exists(Paths.get(filename))

}


/**
 * We create through the companion object
 */

case class StoredFiles(files:List[StoredFile]) extends BasicMarshallable

object StoredFile {

  /*
   * Simple create
   */
  def apply(event: NewFileAdded):StoredFile = StoredFile(event.uuid,0,event.displayName,event.filename,event.filesize,event.messageDate)

  /**
   * Create a DomainEvent from a Command, or an error why we can't
   * @param cmd
   * @return
   */
  def canCreate(fuuid: => Uuid, cmd: AddNewFile):DomainValidation[StoredFileEvent] = {
    Files.exists(Paths.get(cmd.filename)) match {
      case true => NewFileAdded(DateTime.now,fuuid,cmd.displayName, cmd.filename, cmd.filesize).success
      case false => DomainError("File ["  + cmd.filename + "] could not be found)").fail
    }
  }

}
