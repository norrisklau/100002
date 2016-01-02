package tbs.effect.heal

import tbs.effect._
import tbs.heal._
import tbs.entity._

class HealEffect extends EntityEffect {
	var healingFunction : (Entity) => Int = (_) => 0
	var targetAttributeFunction : (Entity) => EntityAttribute = (entity) => entity.attributes.currentHP
	
	def setHealingFunction (hFunc : (Entity) => Int) : Unit = {
	  healingFunction = hFunc
	}
	
	def setAttributeFunction (attrFunc : (Entity) => EntityAttribute) : Unit = {
	  targetAttributeFunction = attrFunc
	}
	
	override def applyTo(entity : Entity) = {
	  val healData = new HealData(targetAttributeFunction, healingAmount = healingFunction(entity))
	  entity.heal(healData)
	}
	
	override def unapplyTo(entity : Entity) = { 
	  
	}
}