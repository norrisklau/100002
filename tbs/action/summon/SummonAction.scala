package tbs.action.summon

/**
 * Summon Actions place a new entity at an unoccupied square. Usually, these creatures
 * are friendly to the summoner, but are controlled by an AI rather than the player themselves. 
 * (Unless the summoner is also an AI, in which case they control both entities)
 * 
 * Entities that are summoned this way generally have a summonEffect attached, which indicates
 * who their summoner was as well as making them susceptible to effects that target summoned
 * creatures, but this is not necessarily the case.
 */
import tbs.action.TileTargetingAction
import tbs.effect.summon.SummonEffect
import tbs.entity._
import tbs.action.Ranged

import scala.collection.mutable.ListBuffer
import tbs.tiles.Tile

class SummonAction extends TileTargetingAction {
  // The function that creates an instance of the entity to be placed
  var entityFunction : () => Entity = () => null
  var summonLimit = 1
  
  /**
   * The entity function is the method that returns the entity to be summoned.
   *  Generally, it will just be a generic factory method with
   * 'CREATE NEW GOBLIN', though you could be more creative if you wanted to.
   */
  def setEntityFunction( func : () => Entity) : Unit = {
    entityFunction = func
  } 
  
  override def isUseable() = {
    // We cannot summon over our limit
    super.isUseable && getSummonedEntitiesInScenario().length < summonLimit
  }
  
  /**
   * Return a list of all entities summoned by this action on the map. 
   * 
   * @return
   * An array with all the entities summoned by the user with this action, on the same
   * map as it.
   */
  def getSummonedEntitiesInScenario(): List[Entity] = {
    getUser().flatMap(_.getScenarioData()) match {
      case Some(scData) => {
        scData.getEntities().filter(_.attachedEffects.filter((effect) => effect match {
          case summonEffect : SummonEffect if (summonEffect.isSourcedFrom(_ == this)) => true
          case _ => false
        }).length > 0)
      }
      case _ => Nil
    }
  }
  
  /**
   * Attach a summon effect, then place the entity on the map.
   * This is a helper function to add necessary effects and whatnot during
   * the execution phase. 
   */
  private def summonEntity(entity : Entity, tile : Tile) = {
    val summonEffect = new SummonEffect
    summonEffect.source_=(this)
    entity.attachEffect(summonEffect)
    tile.placeEntity(entity)
    // scenario.addToTurns(entity)
  }
  
  override def execute() = {
    super.execute()
    if (isValid()) {
      (getTarget(), getTarget().flatMap(_.currentMap), entityFunction()) match {
        case (Some(tile), Some(map), entity) if (entity != null) => {
          assert(tile.getEntity() == None) // If we're overriding an existing creature with a summon, something terrible happened.
          summonEntity(entity, tile)
        }
        case _ => assert (false)
      }
    }
  }
}