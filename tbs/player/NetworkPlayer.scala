package tbs.player

import scala.collection.mutable.ListBuffer

import tbs.entity._
import tbs.action._
import tbs.map._
import tbs.interactive._
import tbs.game.state._
import tbs.party.Party
import tbs.dialog._

/**
 * Networked players. This includes AI players on Server side
 */
class NetworkPlayer extends Player {
  var isConnected : Boolean = false
  @volatile var actionQueue : ListBuffer[Action] = new ListBuffer[Action] 
  
  def ping () : Boolean = {
    false
  }
  
  override def selectAction(entity : Entity) = {
    assert(entity.getCurrentMap() != None)
    // assert (actor.currentScenario != None && actor.currentScenario == this.currentGameState )
    // actor.getCurrentScenario.addDialog ( new AbilityMenu(actor) )
    // Send Message to Network Player (entity.id + actionRequest)
    // while (selectedAction == None && responseTime <= timeOut) { }
    var selectedAction : Action = dequeueAction()
    while (selectedAction == null ) {
      selectedAction = dequeueAction()
    }
    Some(selectedAction)
  }
  
  def getInput() : Unit = {
    
  }
  
  def onPlayerChat() = {
    
  }
  
  /**
   * Queue up actions we're getting from a server / client connection. 
   * Each time selectAction is called locally, it dequeues this queue
   */
  def enqueueAction(action : Action) : Unit = {
    // Make sure we're actually the player
    // assert(action.getUser() != null && action.getUser().getPlayer() == this)
    actionQueue.append(action)
  }
  
  def dequeueAction() : Action = {
    var dequeuedAction : Action = null
    if (! actionQueue.isEmpty) {
      dequeuedAction = actionQueue(0)
      actionQueue.remove(0)
    }
    dequeuedAction
  }
  
}

// From the client standpoint. On the server itself, it will be local or AI players
class ServerPlayer extends NetworkPlayer {
  
}