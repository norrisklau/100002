package test

import io.netty.channel._

import tbs.game.state._
import tbs.game.scenario._
import tbs.game._
import tbs.action._
import tbs.player._

class ServerScenarioGameState (val scenario: ScenarioData) extends Scenario {
  loadScenarioData(scenario)
  
  override def init() = {
    super.init()
	  println(this.toString() + " initialized.")
	  println("Attached game is " + attachedGame + " with client channels:" )
	  for (ch <- attachedGame.getClientChannels()) {
	    initClientChannel(ch)
	  }
  }
  
  def initClientChannel(ch : Channel): Unit  = {
      println("Initializing channel : " +  ch)
    	ch.pipeline().addLast(new ServerScenarioHandler(this))

    	EntityTest.testSerializability(scenarioData)
    	ch.writeAndFlush(new LoadScenarioMessage(scenarioData))
  }
  
  def sendMessageToClients(message : TBSNetMessage) = {
  	for (ch <- attachedGame.getClientChannels()) {
      // EntityTest.testSerializability(message)
      val f = ch.writeAndFlush(message)
      f.addListener(new ChannelFutureListener() {
          override def operationComplete(future: ChannelFuture) {
          }
        })
    }
  }
  
  override def resolveAction(action : Action) {
    sendMessageToClients(new ActionMessage(action))
    super.resolveAction(action)
  }
  
}

class ServerScenarioHandler (serverScenario : ServerScenarioGameState) extends ChannelHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg : Object) = {
    msg match {
      case actionMessage : ActionMessage => {
        val action = actionMessage.getAction()
        action.syncToScenarioData(serverScenario.scenarioData)
        action.getUser().flatMap(_.player) match {
          case Some(player : NetworkPlayer) => {
            player.enqueueAction(action)
          }
         case _ => 
        }
      }
      
      case joinRequest : JoinRequestMessage => {
        val wfFuture = ctx.writeAndFlush(new LoadScenarioMessage(serverScenario.scenarioData))
        wfFuture.addListener(new ChannelFutureListener() {
          override def operationComplete(future: ChannelFuture) {
          }
        })
      }
    }
  }
}