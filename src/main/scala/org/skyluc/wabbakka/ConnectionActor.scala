package org.skyluc.wabbakka

import akka.actor.Actor
import scala.io.Source
import scala.io.Codec
import org.jboss.netty.channel.Channel
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.buffer.ChannelBuffers
import akka.actor.PoisonPill

case class ConnectionConnected(channel: Channel)
case object ConnectionClosed

case class ConnectionInMessage(message: Array[Byte])
case class ConnectionOutMessage(message: Array[Byte])

class ConnectionActor extends Actor with Logger {
  
  private var channel: Channel = _
  
  def receive = {
    case ConnectionConnected(c) =>
      connected(c)
    case ConnectionClosed =>
      closed()
    case ConnectionInMessage(message) =>
      inMessage(message)
    case ConnectionOutMessage(message) =>
      outMessage(message)
  }
  
  def connected(c: Channel) {
    logger.info("=== Connected ===")
    channel= c
  }
  
  def closed() {
    logger.info("=== Closed ===")
    self ! PoisonPill
  }
  
  def inMessage(message: Array[Byte]) {
    logger.info("=== in message: %s ===".format(Source.fromBytes(message)(Codec.UTF8).mkString))
    self ! ConnectionOutMessage(message)
  }
  
  def outMessage(message: Array[Byte]) {
    logger.info("=== out message: %s ===".format(Source.fromBytes(message)(Codec.UTF8).mkString))    
    channel.write(ChannelBuffers.wrappedBuffer(message))
  }

}