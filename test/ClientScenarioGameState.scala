package test

import io.netty.channel._

import tbs.game.state._
import tbs.game.scenario._
import tbs.game._
import tbs.action._
import tbs.player._
import tbs.entity._
  
import scala.collection.mutable.ListBuffer
import scala.util.Random

class ClientScenarioGameState(val serverChannel : Channel, 
    												  val scenario: ScenarioData) extends Scenario {
  scenario.getMap().setZoom(1.0)
  loadScenarioData(scenario)
  
  override def resolveAction(action : Action) {
    action.getUser().flatMap(_.player) match {
      case Some(player) if (! player.isInstanceOf[NetworkPlayer]) => {
        // EntityTest.testSerializability(action)
        val f = serverChannel.writeAndFlush(new ActionMessage(action))
        f.addListener(new ChannelFutureListener {
        override def operationComplete(future : ChannelFuture) {
          }
        })
      }
      case _ =>
    }
    super.resolveAction(action)
  }
  
}

class ClientScenarioHandler (clientScenario : ClientScenarioGameState) extends ChannelHandlerAdapter {
  
  override def channelRead(ctx: ChannelHandlerContext, msg : Object) = {
    msg match {
      case actionMessage : ActionMessage => {
        val action = actionMessage.getAction()
        action.syncToScenarioData(clientScenario.scenarioData)
        println("User is: " + action.getUser())
        action.getUser().flatMap(_.player) match {
          case Some(player : NetworkPlayer) => {
            player.enqueueAction(action)
          }
          case _ =>
        }
      }
      case _ => 
    }
  }
}

