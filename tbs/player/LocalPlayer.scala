package tbs.player

import java.awt.event.KeyEvent

import tbs.entity._
import tbs.action._
import tbs.map._
import tbs.interactive._
import tbs.game.state._
import tbs.party.Party
import tbs.dialog._
import tbs.tiles._


/**
 * Local Human Player
 */
class LocalPlayer extends Player {
  override def selectAction(entity : Entity) = {
    assert(entity.currentScenarioData != None)
    var selectedAction : Option[Action] = None
    // assert (actor.currentScenario != None && actor.currentScenario == this.currentGameState )
    // actor.getCurrentScenario.addDialog ( new AbilityMenu(actor) )
    @volatile var candidateAction : Action = null
    @volatile var actionSelected : Boolean = false
    var selectionDialog : Dialog = null
    
    entity.scenario match {
      case Some(sc) => {
        selectionDialog = new ActionSelectionDialog(entity.getUseableActions(), (chosenAction) => {
	        candidateAction = chosenAction
	        actionSelected = true
	        sc.removeDialog(selectionDialog)
		    })
		
		    sc.addDialog(selectionDialog)
		    while (! actionSelected) {
		    }
		    
		    selectTarget(candidateAction) 
		    
		    if ( candidateAction.isValid()) { // False if player did not select valid targets
		      selectedAction = Some(candidateAction)
		    }
      }
      case _ => 
    }
    
    selectedAction
  }
  
  override def selectTarget(action : Action) : Unit = {
    ( getCurrentScenarioData().flatMap(_.getScenario()) ,  action) match {
      case (Some(sc) , action : TileTargetingAction) => {
        @volatile var tileSelected = false
        @volatile var selectionFinished = false
        // Filter tiles to get only those that are valid targets, highlight, and make the 
        // mouse clicking of them set the target for the action
      }
      case (Some(sc) , action : EntityTargetingAction) => {
        @volatile var entitySelected = false
        sc.enterEntitySelection(action.isValidTarget,
                                (entity: Entity) => { action.setTarget(entity) ; entitySelected = true}, () => {})
        while (! entitySelected) {}
      }
      case _ =>
    }
  }
}

