package tbs.effect

import tbs.action._
import tbs.entity._

import scala.collection.mutable._

/**
 * Effects that reduce the range of movement spells.
 * 
 * Slowfunc is applied on the range of movement actions.
 * For example, (i) => { i / 2 } would halve the movement range of an entity. Range can never be reduced below 0.
 */
class MovementSlowEffect (slowFunc : (Int) => Int) extends EntityEffect {
  // Stores the list of movements we affected, and the range difference we applied.
  // Used when we 'unslow' the actor and restore the movement back to normal
	val affectedActions : HashMap[Action, Int] = new HashMap[Action, Int]
	
	override def applyTo(entity : Entity) = {
	  for (prvSlowEffect <- entity.attachedEffects.filter(_.isInstanceOf[MovementSlowEffect])) {
	    // Override past slows
	    prvSlowEffect.unapplyTo(entity)
	  }
	  val movementActions : Array[Action] = entity.getActions().filter((action) => action.isInstanceOf[MovementAction])
	  for (action <- movementActions) {
	    val mvAction = action.asInstanceOf[MovementAction]
	    val rangeBeforeSlow = mvAction.maximumRange
	    mvAction.maximumRange_=(slowFunc(mvAction.maximumRange))
	    val rangeDeducted = rangeBeforeSlow - mvAction.maximumRange
	    affectedActions(action) = rangeDeducted
	  }
	  super.applyTo(entity)
	}
	
  /**
   * Restores movement by adding range equal to the amount lost during the application of this effect.
   * 
   * This operation should be commutative, in that when multiple slow effects have been applied to 
   * a target, unapplying them in any order should return the target to the original, unslowed 
   * state.
   */
	override def unapplyTo(entity : Entity) = {
	  super.unapplyTo(entity)
	  val actionsToRestore = entity.getActions().filter(affectedActions.keySet.contains(_))
	  for (action <- actionsToRestore) {
	    assert(action.isInstanceOf[MovementAction])
	    val mvAction = action.asInstanceOf[MovementAction]
	    affectedActions.get(mvAction) match {
	      case Some(diff) => mvAction.maximumRange_=(mvAction.maximumRange + diff)
	      case None => assert(false)
	    }
	  }
	}
}