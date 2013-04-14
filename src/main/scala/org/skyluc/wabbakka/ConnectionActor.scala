package org.skyluc.wabbakka

import akka.actor.Actor
import scala.io.Source
import scala.io.Codec
import org.jboss.netty.channel.Channel
import org.jboss.netty.buffer.ChannelBuffer
import org.jboss.netty.buffer.ChannelBuffers
import akka.actor.PoisonPill
import scala.collection.mutable.StringBuilder

object ConnectionActor {

  def formatBytes(count: Int, message: Array[Byte]): String = {
    val builder = new StringBuilder()
    val length = message.length
    var used = 0
    val pad = count % 8
    if (pad != 0) {
      val textBuilder= new StringBuilder(" ")
      0 until pad foreach {
        i =>
          builder.append("   ")
          textBuilder.append(' ')
      }
      val toPrint = Seq(message.length, 8 - pad).min
      0 until toPrint foreach {
        i =>
          builder.append(hexValue(message(used)))
          textBuilder.append(cleanChar(message(used)))
          used += 1
      }
      builder.append(textBuilder)
    }
    while (length - used > 7) {
      val textBuilder= new StringBuilder(" ")
      if (!builder.isEmpty)
        builder.append('\n')
      0 until 8 foreach {
        i =>
          builder.append(hexValue(message(used)))
          textBuilder.append(cleanChar(message(used)))
          used += 1
      }
      builder.append(textBuilder)
    }
    if (used != length) {
      val textBuilder= new StringBuilder(" ")
      if (!builder.isEmpty)
        builder.append('\n')
      while (used < length) {
        builder.append(hexValue(message(used)))
        textBuilder.append(cleanChar(message(used)))
        used += 1
      }
      ((count + length) % 8) to 7 foreach {
        i =>
          builder.append("   ")
          textBuilder.append(" ")
      }
      builder.append(textBuilder)
    }
    builder.toString
  }
  
  private def cleanChar(b: Byte): Char = {
    if (b < ' ')
      '.'
    else
      b.toChar
  }

  private def hexValue(b: Byte): String = {
    " " + hexChar(b >> 4) + hexChar(b & 0xf)
  }

  private def hexChar(b: Int): Char = {
    if (b > 9)
      ('A' + b - 0xA).toChar
    else
      ('0' + b).toChar
  }

}

case class ConnectionStart(port: Int, id: Int)
case class ConnectionConnected(channel: Channel)
case object ConnectionClosed

case class ConnectionInMessage(message: Array[Byte])
case class ConnectionOutMessage(message: Array[Byte])

class ConnectionActor extends Actor with Logger {
  import ConnectionActor._

  private var name: String = _
  private var channel: Channel = _

  private var countIn = 0
  private var countOut = 0

  def receive = {
    case ConnectionStart(p, i) =>
      start(p, i)
    case ConnectionConnected(c) =>
      connected(c)
    case ConnectionClosed =>
      closed()
    case ConnectionInMessage(message) =>
      inMessage(message)
    case ConnectionOutMessage(message) =>
      outMessage(message)
  }

  def start(p: Int, i: Int) {
    name = "Connection-" + p + '.' + i
  }

  def connected(c: Channel) {
    logger.info(name, "connected")
    channel = c
  }

  def closed() {
    logger.info(name, "closed")
    self ! PoisonPill
  }

  def inMessage(message: Array[Byte]) {
    logger.info(name, "received: \n%s".format(formatIn(message)))
    self ! ConnectionOutMessage(message)
  }

  def outMessage(message: Array[Byte]) {
    logger.info(name, "sent: \n%s".format(formatOut(message)))
    channel.write(ChannelBuffers.wrappedBuffer(message))
  }

  def formatIn(message: Array[Byte]): String = {
    val formatted = formatBytes(countIn, message)
    countIn += message.length
    formatted
  }

  def formatOut(message: Array[Byte]): String = {
    val formatted = formatBytes(countOut, message)
    countOut += message.length
    formatted
  }


}