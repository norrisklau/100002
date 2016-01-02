package test

import io.netty.buffer.ByteBuf;

import io.netty.channel._
import io.netty.handler.codec.ReplayingDecoder
import io.netty.handler.codec.serialization
import java.util.Date
import java.util.List

class MessageClientHandler extends ChannelHandlerAdapter {
	override def channelRead(ctx: ChannelHandlerContext, msg: Object) {
	  println("received: " + msg.toString())
	}
}