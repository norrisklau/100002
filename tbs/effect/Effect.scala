package tbs.effect

import scala.collection.mutable.ListBuffer

import tbs.tiles.Tile
import tbs.entity._
import tbs.action._
import tbs.event._
import tbs.map._
import tbs.game.scenario._
import tbs.id.HasID
import tbs.targeting.Targeting 
import tbs.hasSource.HasSource
import tbs.effectqueue.EffectQueue

/**
 * Effects include buffs, debuffs and other changes that 'stick' to tiles and entities and affect
 * they way they behavior.
 */
abstract class Effect extends HasID with Serializable with PartOfScenario with HasSource {
  
  def applyEffect(effectQueue : EffectQueue): Unit
  
  def unapplyEffect(effectQueue : EffectQueue) : Unit
}

/**
 * 
 * Do NOT directly inherit from Effect[T], use TileEffect or EntityEffect instead. If you need an effect
 * that attached to a new type, make a new class TypeEffect that extends Effect[Type], then extend
 * 
 */
abstract class TargetingEffect [T <: HasID] extends Effect {
  
  def applyEffect(effectQueue : EffectQueue): Unit = {}
  
  def unapplyEffect(effectQueue : EffectQueue) : Unit  = {}
  
  protected def applyTo(obj: T) : Unit
  
  def unapplyTo(obj : T) : Unit
}

/**
 * Effect[T] is a bit too general for us, so we'll divide it up into TileEffect, EntityEffect.
 */
class TileEffect extends TargetingEffect[Tile] {
  protected var _attachedTile : Option[Tile] = None
  
  override def applyTo(obj : Tile) = {
    attachedTile_=(Some(obj))
    obj.attachEffect(this)
  }
  
  override def unapplyTo(obj : Tile) = {
    _attachedTile = None
    obj.removeEffect(this)
  }
  
  override def applyEffect(effectQueue : EffectQueue) : Unit = attachedTile match {
    case Some(tile) => {
      applyTo(tile)
    }
    case _ =>
  }
  
  override def unapplyEffect (effectQueue : EffectQueue) = {}
  
  override def scenario = {
    _attachedTile.flatMap( _.getScenarioData.flatMap(_.getScenario()) )
  }
  
  def attachedTile: Option[Tile] = {
    _attachedTile
  }

  def attachedTile_= (tile: Option[Tile]) = {
    _attachedTile = tile
  }
}

class EntityEffect extends TargetingEffect[Entity] {
  var _attachedEntity : Option[Entity] = None
  
  override def applyTo(obj : Entity) = {
    attachedEntity_=(Some(obj))
    obj.attachEffect(this)
  }
  
  override def unapplyTo(obj : Entity) = {
    _attachedEntity = None
    obj.removeEffect(this)
  }
  
  override def applyEffect(effectQueue : EffectQueue) : Unit = attachedEntity match {
    case Some(entity) => {
      applyTo(entity)
    }
    case _ =>
  }
  
  def attachedEntity : Option[Entity] = {
    _attachedEntity
  }
  
  def attachedEntity_= (entity : Option[Entity]): Unit = {
    _attachedEntity = entity
  }
  
  override def scenario() = {
    _attachedEntity.flatMap(_.getScenarioData.flatMap(_.getScenario) )
  }
}

/**
 * Effects that only last a certain number of turns. We track an entity, and 
 * count down at the end of its turn. When the counter reaches 0, the
 * effect is removed. 
 * 
 * It is possible for an entity's turn to never come around (generally when the entity 
 * dies), which means other decorators such as EndsOnDeath should be included if it 
 * really should stop when an entity has stopped living.
 */
abstract trait EndsAfterEntityTurn[T <: HasID] extends TargetingEffect[T] {
  private var _turnCounter : Int = 0
  private var trackedEntity : Option[Entity] = None
  
  /**
   * Set the turn and entity parameters such that the effect is removed after #turns
   * taken by entity have occurred.
   * 
   * If the turnCounter is set to less than 0, this aspect of the effect is ignored
   * and the game never checks to remove this effect for turn based events.
   * 
   * @param numberOfTurns Number of turns before the effect disappears
   * @param entity        Entity for which we reduce the number of turns left at the end of its turn
   */
  def setTurnCounterAndEntity(numberOfTurns : Int, entity : Entity) = {
    turnCounter_=(numberOfTurns)
    setTurnEntity(entity)
  } 
  
  def turnCounter_=(numberOfTurns : Int) = {
    _turnCounter = numberOfTurns
  }
  
  def setTurnEntity(entity : Entity) = {
    trackedEntity = Some(entity)
  }
  
  override abstract def applyTo(obj : T) = {
    super.applyTo(obj)
    
    trackedEntity.flatMap(_.getScenarioData()) match {
      case Some(scData) => {
      }
      case _ =>
    }
    // obj.getScenarioData().get.onEvent(event) if (function) {duration -= 1; if duration == 0}
  }
  
  override abstract def unapplyTo(obj : T) = {
    super.applyTo(obj)
  }
}


