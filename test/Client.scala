package test

import java.io._

import io.netty.bootstrap.Bootstrap

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.codec.serialization.ClassResolvers

object Client {
  def main(args: Array[String]) {
    val host = "127.0.0.1"
    val port = 9000
    new Client(host, port).run()
  }
}

class Client(host: String, port: Int) {
  def run() {
    val group = new NioEventLoopGroup
    try {
      val bootstrap = new Bootstrap
      val ci = new ChannelInitializer [SocketChannel] () {
        override def initChannel (ch: SocketChannel) : Unit = {
          ch.pipeline().addLast(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)), new MessageClientHandler)
        }
      }
      bootstrap.group(group).channel(classOf[NioSocketChannel]).handler(ci)
      val f = bootstrap.connect(host, port).sync()
      f.channel().closeFuture().sync()
    } finally {
      group.shutdownGracefully()
    }
  }
}