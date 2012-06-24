package org.skyluc.wabbakka

import akka.actor.Actor
import java.nio.channels.SocketChannel
import java.nio.channels.ServerSocketChannel
import java.net.InetSocketAddress
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels
import akka.actor.Props

case class InSocketStart(port: Int)

class InSocketActor extends Actor with Logger {

  var socket: ServerSocketChannel = _

  def receive = {
    case InSocketStart(port) =>
      val factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool)
      val bootstrap = new ServerBootstrap(factory)

      bootstrap.setPipelineFactory(new ChannelPipelineFactory {
        def getPipeline() = Channels.pipeline(new ConnectionHandler(createConnectionActor))
      })

      bootstrap.setOption("child.tcpNoDelay", true)
      bootstrap.setOption("child.keepAlive", true)

      bootstrap.bind(new InetSocketAddress(port))

      logger.info("==== In Socket Started on %d ====".format(port))
  }
  
  def createConnectionActor = context.actorOf(Props[ConnectionActor])

}