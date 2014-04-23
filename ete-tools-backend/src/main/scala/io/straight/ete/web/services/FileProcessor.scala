package io.straight.ete.web.services

import io.straight.ete.web.messages._
import io.straight.ete.web.model.StoredFile
import io.straight.fw.service._
import com.escalatesoft.subcut.inject.{AutoInjectable, BindingModule}
import io.straight.ete.web.messages.AddNewFile
import io.straight.ete.web.messages.NewFileAdded
import org.joda.time.DateTime
import io.straight.fw.akka.ActorSupport
import akka.actor.Props
import io.straight.fw.model.{Uuid, IdGenerator, UuidGenerator}
import scala.reflect.ClassTag
import io.straight.ete.web.messages.NewFileAdded
import io.straight.ete.web.messages.ChangeFile
import io.straight.ete.web.messages.AddNewFile
import scala.Some
import io.straight.ete.web.messages.FileContentChanged


/**
 *
 * @author rbuckland
 */
class FileProcessor()
                   (implicit val bindingModule: BindingModule)
  extends UuidAbstractProcessor[StoredFile,StoredFileEvent, StoredFileCommand]
  with AutoInjectable
  {

  val repository = inject [UuidRepository[StoredFile]]

  val idGenerator = inject [UuidGenerator[StoredFile]]

  /*
   * This method is called on recovery as well as normal creation.
   * i.e This is the one to one mapping between an Event and Creation or modification
   * of a Domain Object. I guess .. tread carefully here. It must be self contained.
   */
  override def eventToDomainObject(event: StoredFileEvent):StoredFile = {
    event match {
      case e: NewFileAdded => StoredFile(e)
      case e: FileContentChanged => repository.getByKey(e.uuid)
        .get.copy(filename = e.newFilename, changedDateTime = Some(e.messageDate))
    }
  }

  val processCommand: Receive = {

    case cmd : AddNewFile => process { StoredFile.canCreate(idGenerator.newId,cmd) }
    case cmd : ChangeFile => process {
      for {
        obj <- ensureVersion(cmd.uuid,Option(cmd.expectedVersion))
      } yield FileContentChanged(DateTime.now,obj.id,obj.displayName,cmd.newFilename)
    }
  }

}

object FileProcessor extends ActorSupport {
  def props: Props =  Props(classOf[FileProcessor])
}

/**
 * A Service class - a wrapper around out repo to make it safe for every one
 * @param bindingModule
 */
class FileService()
                 (implicit val bindingModule: BindingModule)
  extends UuidAbstractService[StoredFile]
  with AutoInjectable {
  val repository = injectOptional [UuidRepository[StoredFile]].get
}

