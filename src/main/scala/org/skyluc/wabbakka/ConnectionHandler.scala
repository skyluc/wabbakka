package org.skyluc.wabbakka

import org.jboss.netty.channel.SimpleChannelHandler
import org.jboss.netty.channel.ChannelHandlerContext
import org.jboss.netty.channel.ChannelStateEvent
import org.jboss.netty.channel.MessageEvent
import org.jboss.netty.buffer.ChannelBuffer
import java.nio.charset.Charset
import akka.actor.ActorRef

object ConnectionHandler {
  val charset= Charset.forName("UTF-8")
}

class ConnectionHandler(connection: ActorRef) extends SimpleChannelHandler {
  
  import ConnectionHandler._
 
  override def channelConnected(context: ChannelHandlerContext, event: ChannelStateEvent) {
    connection ! ConnectionConnected(event.getChannel)
  }
  
  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    val channelBuffer= event.getMessage.asInstanceOf[ChannelBuffer]
    val message= new Array[Byte](channelBuffer.readableBytes)
    channelBuffer.readBytes(message)
    connection ! ConnectionInMessage(message)
  }
  
  override def channelClosed(context: ChannelHandlerContext, event: ChannelStateEvent) {
    connection ! ConnectionClosed
  }
  
  
  
}