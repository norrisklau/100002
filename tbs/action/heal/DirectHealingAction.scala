package tbs.action.heal

/**
 * Basic heal that immediately recovers a chunk of a creature's current HP.
 */
import tbs.action.EntityTargetingAction
import tbs.entity._
class DirectHealingAction extends EntityTargetingAction {
	var healingFunction : (Entity, Entity) => Int = (user, target) => 0
	
	def setHealingFunction(hFunc : (Entity, Entity) => Int) = {
	  healingFunction = hFunc
	}
	
	import tbs.heal.HealData
	override def execute() = {
	  (getUser(), getTarget(), getTarget().flatMap(_.getScenarioData()) ) match {
	    case (Some(usr), Some(tar) , Some(sc)) => 
	      val healingData = new HealData(_.attributes.currentHP, healingFunction(usr, tar))
	      tar.heal(healingData)
	    case _ =>
	  }  
	}
}