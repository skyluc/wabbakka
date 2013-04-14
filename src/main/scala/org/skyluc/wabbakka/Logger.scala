package org.skyluc.wabbakka

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.ActorRef
import akka.actor.Props

class LoggerActor extends Actor {

  def receive = {
    case s: String =>
      println(s)
  }

}

object Logger {

  var actor: ActorRef = _

  def init(system: ActorSystem) {
    actor = system.actorOf(Props[LoggerActor])
  }

  def info(name: String, message: String) {
    Logger.actor ! "%s: %s".format(name, message)
  }
}

trait Logger {

  val logger = Logger

}