package tbs.player

import java.awt.event.KeyEvent
import java.util.UUID

import tbs.entity._
import tbs.action._
import tbs.map._
import tbs.interactive._
import tbs.game.state._
import tbs.party.Party
import tbs.dialog._
import tbs.game.scenario.ScenarioData

abstract class Player extends Serializable {
  var name : String = ""
  var id : String = this.getClass().toString() + UUID.randomUUID()
    
  // Party information
  var party : Party = new Party
  
  var currentScenarioData : Option[ScenarioData] = None
  
  def getCurrentScenarioData() : Option[ScenarioData] = {
    currentScenarioData
  }
  
  /**
   * Choose an action for the creature, select targets for the action, and 
   * pass the action for resolution.
   * 
   * AI players will block and use conventional if branching to choose actions and targets.
   * GUI (local) players will use pop up dialogue/selection boxes.
   * 
   * @param entity
   * Entity the player is choosing actions for.
   * 
   * @return
   * None if no action was chosen. 
   * 
   * Some(Action) if an action was selected, but the action parameters (targets) may not be
   * properly filled in, resulting in a partially complete Action that cannot be used.
   * 
   */
  def selectAction(entity : Entity) : Option[Action]
  
  /**
   * Called during the selectAction method, to select Action targets. May be cancelled,
   * leaving the action invalid for use. (This will be checked by the Scenario)
   */
  protected def selectTarget(action: Action) : Unit = {}
}




