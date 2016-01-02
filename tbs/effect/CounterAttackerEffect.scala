package tbs.effect

import tbs.entity._
import tbs.action._

/**
 * Apply to entities to give them a counter-attack ability.
 * 
 * Due to the fact that the counter-attack is itself an attack ability, we should
 * make sure that the counter-attack does NOT trigger off counter-attacks, or else
 * it is possible that two actors could counter attack each other in a loop until 
 * one or the other dies (which is not what we want, probably).
 * 
 * 
 */
class CounterAttackerEffect extends EntityEffect {
  var onActionAfterFunc : (Action) => Unit = (a) => {}
  
  override def applyTo(e : Entity) = {
    onActionAfterFunc = (action: Action) => {
      (action, action.getUser()) match {
        /**
         * Counter-Attack procs on PROACTIVE weapon attacks. This precludes Counter-Attack
         * triggering on a Counter-Attack (since this is a reactive attack)
         */
        case (attack : WeaponAttack with Proactive , Some(attacker)) => {
          if (attack.getTarget() == e) {
            val counter = new CounterAttack()
            counter.setUser(e)
            counter.setTarget(attacker)
            
            if (counter.isValid() && counter.isUseable()) {
              counter.resolve();
            }
          }
        }
        case _ => 
      }
    }
    
    e.getScenarioData() match {
      case Some(scData) => {
        scData.onAfterAction.addFunction(onActionAfterFunc)
        e.attachEffect(this)
      }
      case _ => 
    }
  }
  
  /**
   * Remove from an entity, so they no longer counter attack. 
   */
  override def unapplyTo(e : Entity) = {
    e.getScenarioData() match {
      case Some(scData) => {
		    scData.onAfterAction.removeFunction(onActionAfterFunc)
		    e.removeEffect(this)
      }
      case _ =>
    }
  }
}