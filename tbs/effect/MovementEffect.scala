package tbs.effect

import tbs.movement.MovementData
import tbs.effectqueue.EffectQueue
import tbs.game.scenario.Scenario
import tbs.tiles.Tile

class MovementEffect (var movementData : MovementData)  extends Effect {
  
  override def applyEffect(effectQueue : EffectQueue) : Unit = movementData.getPath() match {
    case (tile : Tile) :: rest => (tile.currentMap, movementData.mover.scenario) match {
      case (Some(map) , Some(scenario))  => {
        // Move first tile
        map.moveEntity(movementData.mover, tile)
        // Move the rest of the path
        val nextLeg : MovementEffect = new MovementEffect(movementData.increment)
        nextLeg.source_=(this)
        effectQueue.enqueue( nextLeg )
      }
      case _ => 
    }
    case Nil => 
  }
  
  override def scenario: Option[Scenario] = {
    movementData.mover.getScenarioData().flatMap(_.getScenario)
  }
  
  // You can't really unapply movement as an effect. (
  override def unapplyEffect(effectQueue : EffectQueue) : Unit = {
    
  }
}