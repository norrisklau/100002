package tbs.action

import tbs.entity._
import tbs.effect.Effect

/**
 * A generic ranged attack. On a hit, some damage is dealt, nothing happens on a miss (though some
 * events may trigger)
 */
class RangedAttackAction extends Attack with Ranged {
  import tbs.effect.DamageEffect;
  
  val createDamageEffect : () => Effect = () => {
    val d = new DamageEffect
    d.setDamageFunction((entity) => 10)
    d.attachedEntity_=(target)
    d
  }
  
  addHitEffect(createDamageEffect)
  
  override def isValidTarget(candidate : Entity) : Boolean = {
    getUser() match {
      case Some(user) => {
        user.xyMapDistanceToEntity(candidate) >= minimumRange &&
        user.xyMapDistanceToEntity(candidate) <= maximumRange &&
        user != candidate
      }
      case None => false   							  
    }
  }
  
  override def setUser(user : Entity) = {
    super.setUser(user)
  }
  
  override def resolveHit() = {
    super.resolveHit()
  }
}