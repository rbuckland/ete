package io.straight.ete.web.routes


import akka.actor.{Props, ActorSystem, Actor}
import spray.routing.directives.DebuggingDirectives
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import io.straight.ete.web.{BindingKeys}
import io.straight.ete.web.services.{EteExtractorActor, FileService, FileProcessor}
import io.straight.fw.marshalling.JacksonMapper
import akka.util.Timeout

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class CoreWebRouter()
                   (implicit val bindingModule: BindingModule)
  extends Actor
  with Injectable
  with FileRouter
  with H2DatabaseGenerator
  with EteTransformRouter
  with StaticDocRouter
  with DebuggingDirectives
  {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  override val timeout = inject[Timeout](BindingKeys.DefaultTimeout)
  val fileService = inject[FileService]

  val eteExtractorActor = context.system.actorOf(Props[EteExtractorActor],"eteExtractorActor")
  val fileProcessor = context.actorSelection(FileProcessor.actorPath)

  object MyJacksonMapper extends JacksonMapper
  
  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(
    logRequestResponse("ete-logger") {
      pathPrefix("api") {
         apiFilesRoute ~ generateH2DbRoute ~ apiEteTransform
      }
    } ~
    staticFilesRoute
  )
}