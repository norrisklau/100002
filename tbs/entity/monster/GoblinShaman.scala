package tbs.entity.monster

/**
 * Goblin Shamans can heal their allies and summon feral creatures to fight
 * their enemies.
 * 
 * Healing Touch: Cheap healing spell
 * Summon Wolf: Can summon a wolf to aid him (a fodder minion, maximum of 1)
 */
import tbs.entity.Entity
class GoblinShaman extends Entity {
  commonNoun = "Goblin Shaman"
	glyph = 's'
}

class ShamanHealingAction extends tbs.action.heal.DirectHealingAction {
  setHealingFunction((user, target) => user.attributes.currentMagicPower.getValue().toInt)
}