package tbs.action.trap

import tbs.effect._
import tbs.action.Proactive
import tbs.action.TileTargetingAction
import tbs.action.Ranged
import tbs.entity.Entity
import tbs.tiles._

/**
 * Actions which place a trap in a tile. Typically, these traps are triggered by movement near the tiles
 * they are placed in, but you are free to make your own triggers for traps.
 * 
 * For example, a trap may trigger on enemies casting healing spells near it, etc. to make
 * for more interesting abilities. Check the TileTrapEffect classes for more information.
 */
class PlaceTileTrapAction (private var makeTrap : () => TileTrapEffect) extends TileTargetingAction with Proactive with Ranged {
  minimumRange_=(1)
  maximumRange_=(1)
  
  // Set the function that creates the right trap whenever we are called to place a new one on the map
  def setMakeTrapFunc ( trapF : () => TileTrapEffect) : Unit = {
    makeTrap = trapF
  }
  
	override def isValidTarget(tile : TacticalTile) : Boolean = {
	  // Cannot place a trap in a square with a friendly trap. 
	  getUser() match {
	    case Some(user) => {
	      tile.getEntity() == None &&
	      tile.attachedEffects.filter((effect) => effect.isInstanceOf[TileTrapEffect]).isEmpty &&
	      user.xyMapDistanceToTile(tile) <= maximumRange
	    }
	    case _ => println("No User"); false
	  }
	}
	
	override def execute() = {
	  // Attach a trap to the targetted tile
	  if (isValid()) {
	    getTarget() match {
	      case Some(tile) => {
	        val trap = makeTrap()
	        assert(trap != null)
	        trap.setTrapLayer(getUser().get)
	        // tile.getScenarioData().get.removeTileEffect(getUser() == _.asInstanceOf[TileTrapEffect].getTrapLayer())
          // tile.getScenarioData().pushEffect(trap)
          tile.getOtherTilesInMap().foreach(_.removeEffect (_.asInstanceOf[TileTrapEffect].getTrapLayer() == getUser()) )
	        trap.applyTo(tile)
	        println(getUser() + " placed a trap at " + tile)
	      }
	      case None => System.err.println("Error: " + PlaceTileTrapAction.this.toString() + " attempted to execute, but no target was set.")
	    }
	  } else {
	    System.err.println("Error: " + PlaceTileTrapAction.this.toString() + " attempted to execute, but it was invalid.")
	  }
	}
	
	/**
	 * When we set a user for this, we also make it the trap layer for our trap effect we apply.
	 */
	override def setUser(newUser : Entity) = {
	  super.setUser(newUser)
  }
}