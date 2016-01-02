package test

import tbs.game._
import tbs.game.state._
import tbs.dialog._
import tbs.game.scenario._

import io.netty.buffer.ByteBuf;
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.ReplayingDecoder
import io.netty.handler.codec.serialization
import java.util.List

object TestMain {
  def main(args : Array[String]) : Unit = {
    val game : Game = new Game
    val cake = new TitleScreen
    cake.init()
    // Initialise JOGL and other resources
    game.pushState(cake)
    val testTitle = new TestTitleMenu
    testTitle.addComponent(new DialogButton(() => {
	  	new Thread(new Runnable() {
	    	  override def run() : Unit = {
	    	    new TestClient("127.0.0.1", 9000, game).run()
	    	  }
	    	}).start()
	  	}  ,
	    0.1,
	    0.1,
	    0.8,
	    0.3
	  ))
	  testTitle.addComponent(new DialogButton(() => {
	      println("Starting Server ...")
	      var ch : Channel = null
	      new Thread(new Runnable() {
	    	  override def run() : Unit = {
	    	    new TestServer(9000, game).run()
	    	  }
	    	}).start()
	    	game.pushState(new ServerScenarioGameState(new ScenarioData))
	  	} ,
	    0.5, 
	    0.5,
	    0.8,
	    0.3
	  ))
    cake.addDialog(testTitle)
    game.init()
  }
}

class TestTitleMenu extends DialogMenu {
  setGlCoords(-0.9, -0.9)
  setGlDimensions(1.8, 1.8)
}

class TestServer(port : Int, val attachedGame : Game) {
  def run() : Unit = {
    println("Server is running!")
    val bossGroup : EventLoopGroup = new NioEventLoopGroup()
    val workerGroup : EventLoopGroup = new NioEventLoopGroup()
    try {
      val bootstrap = new ServerBootstrap()
      val ci = new ChannelInitializer [SocketChannel] () {
        override def initChannel (ch: SocketChannel) : Unit = {
          ch.pipeline().addLast(new MyEncoder, 
                                new MyDecoder, 
                                new TestServerHandler(attachedGame))
        }
      }
      bootstrap.group(bossGroup, workerGroup).channel(classOf[NioServerSocketChannel]).childHandler(ci)
      val f = bootstrap.bind(port).sync()
      f.channel().closeFuture().sync()
    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}

class MyEncoder extends ObjectEncoder {
  override def encode(ctx: ChannelHandlerContext, msg : java.io.Serializable, out : ByteBuf) = {
    super.encode(ctx, msg, out)
  }
}

class MyDecoder extends ObjectDecoder(ClassResolvers.softCachingConcurrentResolver(null)) {
  override def decode(ctx: ChannelHandlerContext, in : ByteBuf) : Object = {
    super.decode(ctx, in)
  }
}

class TestServerHandler(val attachedGame : Game) extends ChannelHandlerAdapter {
  override def channelActive (ctx : ChannelHandlerContext) = {
  	println("Server : " + ctx + " is active.")
  }
  
  override def channelRead(ctx: ChannelHandlerContext, msg: Object) = {
    msg match {
      case jm : JoinRequestMessage => {
        println("Adding channel to game : " + ctx.channel())
        attachedGame.addClientChannel(ctx.channel())
      }
      case _ => ctx.fireChannelRead(msg)
    }
  }
}

class TestClient(host: String, port: Int, game: Game) {
  val bootstrap : Bootstrap = null
  
  def run() {
    val group = new NioEventLoopGroup
    try {
      val bootstrap = new Bootstrap
      val ci = new ChannelInitializer [SocketChannel] () {
        override def initChannel (ch: SocketChannel) : Unit = {
          ch.pipeline().addLast(new MyEncoder, 
                                new MyDecoder,
                                new ClientJoinHandler(game))
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

class ClientJoinHandler(val attachedGame : Game) extends ChannelHandlerAdapter {
  override def channelActive (ctx : ChannelHandlerContext) = {
    println("Connected to " + ctx)
    val writeFuture = ctx.writeAndFlush(new JoinRequestMessage)
  }
  
  // Listen to scenario send states
  override def channelRead(ctx : ChannelHandlerContext, msg: Object) {
    msg match {
      case lsm: LoadScenarioMessage => {
        ctx.pipeline().remove(this)
        val sc = lsm.getScenarioInformation()
        val scenarioGameState = new ClientScenarioGameState(ctx.channel(), sc)
        ctx.pipeline().addLast(new ClientScenarioHandler(scenarioGameState))
        attachedGame.pushState(scenarioGameState)
      }
      case _ => // We don't respond to any other messages atm
    }
    ctx.fireChannelRead(msg)
  }
}
