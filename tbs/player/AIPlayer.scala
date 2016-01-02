package tbs.player

import tbs.entity._
import tbs.action._
import tbs.map._
import tbs.interactive._
import tbs.game.state._

import scala.util.Random
import java.util.Date

class AIPlayer extends Player {
  var random = new Random()
  
  override def selectAction(entity : Entity) : Option[Action] = {
    assert(entity.getCurrentMap != None)
    var candidateAction : Option[Action] = None
    // If we have useable actions
    val useableActions = entity.actions.filter( (action) => {action.isUseable()} )
    if (! useableActions.isEmpty ) {
      candidateAction = Some(useableActions(random.nextInt(useableActions.size)))
      selectTarget(candidateAction.get)
    }
    // assert (actor.currentScenario != None && actor.currentScenario == this.currentGameState )
    // actor.getCurrentScenario.addDialog ( new AbilityMenu(actor) )
    val time = new Date().getTime()
    while (new Date().getTime() - time < 100) {}
    candidateAction
  }
  
  override def selectTarget(action : Action) : Unit = {
    assert(currentScenarioData != None)
    action match {
      case a : TileTargetingAction => {
        val tars = a.getValidTargets()
        if (! tars.isEmpty) {
          a.setTarget(tars(random.nextInt(tars.size)))
        }
      }
      case b : EntityTargetingAction => {
        val tars = b.getValidTargets()
        if (! tars.isEmpty) {
          b.setTarget(tars(random.nextInt(tars.size)))
        }
      }
      case _ =>
    }
  }
}

class RandomAIPlayer extends AIPlayer {
  
}