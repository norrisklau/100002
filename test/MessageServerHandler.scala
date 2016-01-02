package test

import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelHandlerAdapter
import io.netty.channel._
import io.netty.handler.codec.MessageToByteEncoder

import java.util.Date


class MessageServerHandler extends ChannelHandlerAdapter {
  var active = true
  
  override def channelActive(ctx : ChannelHandlerContext) : Unit = {
    println("Channel is active: " + ctx.toString())
    active = true
    var time = System.currentTimeMillis()
    sendTime(ctx)
    while (active) {
      if (System.currentTimeMillis() - time >= 4000) {
        time = System.currentTimeMillis()
        sendTime(ctx)
      }
    }
  }
  
  override def channelRegistered(ctx : ChannelHandlerContext) : Unit = {
    System.out.println(this + " is registered to " + ctx)
  }
  
  override def channelInactive(ctx : ChannelHandlerContext) : Unit = {
    System.out.println("Channel is inactive.")
    active = false
  }
  
  def sendTime(ctx: ChannelHandlerContext) = {
    val writeFuture = ctx.writeAndFlush(121)
    // Close Connection after write I/O finished
    val writeListener = new ChannelFutureListener() {
      override def operationComplete(future: ChannelFuture) : Unit = {
      }
    }
    writeFuture.addListener(writeListener)
  }
  
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    ctx.close();
  }
}