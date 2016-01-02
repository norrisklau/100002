package tbs.effect

import tbs.entity._

/**
 * A 'buff' on an entity that redirects incoming damage to hp to mp.
 */
class ManaShieldEffect extends EntityEffect {
  import tbs.damage.DamageData;
  
  var redirectFunc : (DamageData) => Unit = (d) => {}
  
  override def applyTo(entity : Entity) = {
    // Triggers before damage is applied
    redirectFunc = (d: DamageData) => {
      // If the damage is targeting the hp of the entity we've buffed
      if (d.getTargetAttribute(entity) == entity.attributes.currentHP) {
        val currMP = entity.attributes.currentMP
        if (currMP.getValue() >= d.value)  
          d.attributeF = _.attributes.currentMP
        else if (currMP.getValue() < d.value && currMP.getValue() > 0) {
        	// Reduce HP damage by our MP
          val difference = d.value - currMP.getValue()
          d.value = difference
          entity.applyDamage(new DamageData(_.attributes.currentMP, currMP.getValue())) 
        }
      }
    }
    
    scenario match {
      case Some(scData) => {
        // Rework
      }
      case _ => 
    }

  }
   
  override def unapplyTo(e : Entity) = {
     scenario match {
      case Some(scData) => {
        
        e.removeEffect(this)
      }
      case _ => 
    }
  } 
  
}