package io.straight.ete.web.messages

import org.joda.time.DateTime
import io.straight.fw.model.Uuid
import io.straight.fw.messages.{BaseCommand, BaseEvent}

/**
 * @author rbuckland
 */


abstract class StoredFileEvent(override val messageDate: DateTime) extends BaseEvent(messageDate)

abstract class StoredFileCommand(override val messageDate: DateTime) extends BaseCommand(messageDate)

case class AddNewFile(
                       override val messageDate: DateTime = DateTime.now,
                       filename: String, displayName: String, filesize: Int) extends StoredFileCommand(messageDate)

case class NewFileAdded(
                         override val messageDate: DateTime  = DateTime.now,
                        uuid: Uuid, displayName: String, filename: String, filesize: Int) extends StoredFileEvent(messageDate)

/**
 * Call this COmmand when you want to swap just the file. (like save.. new contents)
 * @param uuid
 * @param expectedVersion
 * @param newFilename
 */
case class ChangeFile(
                       override val messageDate: DateTime  = DateTime.now,
                       uuid: Uuid, expectedVersion: Long, newFilename: String
                       ) extends StoredFileCommand(messageDate)

/**
 * This is the event that fired as a result of a new file being pushed in (we just work with "filenames"
 * and not so much the content
 *
 * @param uuid
 * @param newFilename
 * @param datetimeChanged
 */
case class FileContentChanged( override val messageDate: DateTime  = DateTime.now,
                               uuid: Uuid, displayName: String, newFilename: String) extends StoredFileEvent(messageDate)


