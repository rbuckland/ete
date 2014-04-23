package io.straight.ete.web.config

import akka.actor.{ActorSystem, ExtendedActorSystem, ExtensionIdProvider, ExtensionId}

/**
 * @author rbuckland
 */
object Settings {

  val CoreActorSystemName = "ete-web"
  val ActorSysBindingKey = ("ActorSystem-" + CoreActorSystemName) intern

}