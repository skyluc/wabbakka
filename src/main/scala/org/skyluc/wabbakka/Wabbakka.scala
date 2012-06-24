package org.skyluc.wabbakka

import akka.actor.ActorSystem
import akka.actor.Props
import akka.dispatch.ExecutionContext
import scala.util.Properties

object Wabbakka extends Logger {

  def main(args: Array[String]) {
    
    val port = Properties.envOrElse("PORT", "8080").toInt
    
    implicit val system= ActorSystem()
    
    Logger.init(system)
    
    logger.info("===== App Started =====")
    
    system.actorOf(Props[InSocketActor], "listeningSocket") ! InSocketStart(port)
    
  }

}