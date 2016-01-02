package tbs.effect

import tbs.damage._
import tbs.entity._

/**
 * Deals damage to an entity when applied
 */
class DamageEffect (var damageFunction : (Entity) => Int = (_) => 0)extends EntityEffect {
  import tbs.entity._
	var damage : DamageData = null
	
	def setDamageFunction(dmgF : (Entity) => Int) = {
    damageFunction = dmgF
  }
	
	override def applyTo(entity : Entity) = {
	  damage = new DamageData(_.attributes.currentHP, damageFunction(entity))
	  entity.applyDamage(damage)
	}
	
	/**
	 * I'm not sure if you ever want to use this one, but 
	 * unapplying damage is the same as healing for the same amount (though
	 * the entity may be *dead* by this point.)
	 */
	override def unapplyTo(entity : Entity) = {
	  // entity.applyHealing(healing)
	}
}