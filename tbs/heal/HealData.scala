package tbs.heal

import tbs.entity._

/**
 * A 'packet' of healing data passed to the entity.heal method. 
 * The fields are rather simple: a method that returns the attribute of the target entity to be healed
 * (e.g. _.attributes.CurrentHP) and the value of the heal as an integer.
 */
class HealData (var targetAttributeF : (Entity) => EntityAttribute, var healingAmount : Int = 0){
  
}