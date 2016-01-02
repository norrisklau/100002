package tbs.action.shot

import tbs.action._
import tbs.id.HasID

/**
 * Base class for ranged shots that deal damage and slow the target on a hit. By default, nothing happens to the 
 * target on a miss.
 */
class SnareShotAction(slowFunc : (Int) => Int, slowDuration : Int)  extends RangedAttackAction {
  import tbs.effect._
  import tbs.entity._

  val slowEffect = new MovementSlowEffect(slowFunc) with EndsAfterEntityTurn[Entity]
  slowEffect.source_=(Some(this))
  slowEffect.turnCounter_=(slowDuration)
  addHitEffect(() => slowEffect)
  
	override def resolveHit() = {
	  super.resolveHit()
	}
  
  override def setTarget(target : Entity) = {
    super.setTarget(target)
    slowEffect.setTurnEntity(target)
  }
}