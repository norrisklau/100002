package tbs.damage

import tbs.entity._

class DamageData (var attributeF : (Entity) => EntityAttribute, var value : Double) {
  def getTargetAttribute(entity : Entity) : EntityAttribute = {
    attributeF(entity)
  }
}