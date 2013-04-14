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
import akka.pattern.ask
import akka.util.Timeout
import akka.util.duration._
import akka.actor.ActorRef
import akka.dispatch.Await

case class InSocketStart(port: Int)
case object NewConnectionActor

class InSocketActor extends Actor with Logger {

  var name: String = _
  var port: Int = _

  var connectionCount = 0

  def receive = {
    case InSocketStart(p) =>
      port= p
      name = "InSocket-" + port
      val factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool)
      val bootstrap = new ServerBootstrap(factory)

      bootstrap.setPipelineFactory(new ChannelPipelineFactory {
        def getPipeline() = {
          implicit val timeout = Timeout(500 millis)
          val future = (self ? NewConnectionActor).mapTo[ActorRef]
          val connectionActor = Await.result(future, 500 millis)
          Channels.pipeline(new ConnectionHandler(connectionActor))
        }
      })

      bootstrap.setOption("child.tcpNoDelay", true)
      bootstrap.setOption("child.keepAlive", true)

      bootstrap.bind(new InetSocketAddress(port))

      logger.info(name, "started")
    case NewConnectionActor =>
      val connectionActor = context.actorOf(Props[ConnectionActor])
      connectionCount += 1
      connectionActor ! ConnectionStart(port, connectionCount)
      sender ! connectionActor
  }

}