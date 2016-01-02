package tbs.entity.monster

import tbs.entity._
import tbs.action._

/**
 * Goblin trappers harry their foes from a distance, laying traps and snaring their opponents to keep
 * them away.
 * 
 * HP : Medium
 * Defences : Low
 * Attack : Medium
 * Movement : Medium
 * 
 */
class GoblinTrapper extends Entity {
  commonNoun = "Goblin Trapper"
  glyph = 'g'
  /** 
   *  Starting abilities (Level 0)
   */
  import tbs.action.trap.PlaceBearTrapAction
	addActionToLearnSet( (0, new BasicMovementAction(movementRange = 6)) )
  import tbs.action.shot.SnareShotAction
  // A snare shot with 3 range, that slows movement by 2 for one turn
  val snareShot = new SnareShotAction(slowFunc = _ - 3, slowDuration = 1)
  snareShot.setRange(1, 5)
  snareShot.setUser(this)
	addActionToLearnSet(0, snareShot) 
  // Place a bear trap that deals a low amount of damage and stops movement when a creature walks on top of it
	addActionToLearnSet( (0, new PlaceBearTrapAction(dmgFunc = (_) => 10, stopDuration = 0))) 
	
	for (action <- this.getActionSetOnLevel(1)) {
	  this.giveAction(action)
	}
}