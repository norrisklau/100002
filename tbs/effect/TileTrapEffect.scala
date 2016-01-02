package tbs.effect

import tbs.map._
import tbs.movement._
import tbs.action._
import tbs.entity._
import tbs.damage.DamageData
import tbs.game.scenario._
import tbs.rendering._

import tbs.event.Trigger
import tbs.event.{TBSEvent, BeforeEffectApplyEvent, AfterEffectApplyEvent}
import tbs.tiles.Tile

/**
 * Attach to a tile to create a trap that triggers when certain conditions are met. Generally
 * these traps are triggered when a target moves over the, but frankly, they could trigger
 * on anything if you wanted.
 */
class TileTrapEffect extends TileEffect with Renderable {
  private var trapLayer : Option[Entity] = None
  
  def getTrapLayer() : Option[Entity] = {
    trapLayer
  }
  
  def setTrapLayer(entity : Entity) = {
    trapLayer = Some(entity)
  }
  
	override def applyTo(tile : Tile) : Unit = {
	  super.applyTo(tile)
	}
	
	override def unapplyTo(tile : Tile) : Unit = {
	  super.unapplyTo(tile)
	}
}

/**
 * Bear traps are simple traps that damage creatures that move over them.
 */
class BearTrapEffect (dmgFunc : (Entity) => Int, stopDuration : Int) extends TileTrapEffect {
  
  var trigger : Trigger = _
  
  val movementTriggerFunction : (TBSEvent) => Unit = (tbsEvent) => tbsEvent match {
    case AfterEffectApplyEvent(mvEffect : MovementEffect) => {
      assert (mvEffect.movementData.getScenarioData() == attachedTile.flatMap(_.getScenarioData()) )
      val mover = mvEffect.movementData.mover
      attachedTile.flatMap(_.scenario) match {
        case Some(sc) if (mover.currentTile == this.attachedTile)=> {
          System.err.println(this + " was triggered at tile " + attachedTile + " by entity " + mover)
          // Push damage to entity
          val trapDamage = new DamageEffect(_ => 10)
          trapDamage.attachedEntity_=(Some(mover))
          trapDamage.source_=(Some(this))
          sc.enqueueEffect(trapDamage)
          
          val stopMovement = new NegationEffect(this.scenario.get, _.isSourcedFrom(_ == mvEffect))
          sc.enqueueEffect(stopMovement)
          
          sc.resolveEffects()  
          sc.getScenarioData().removeTrigger(trigger)
          unapplyEffect(sc.effectQueue)
        }
        case _ => 
      }

    }
    case _ => 
  }
  
  override def applyTo(tile : Tile) : Unit = {
    tile.getScenarioData() match {
      case Some(sc) => {
        trigger = new Trigger(movementTriggerFunction)
        sc.addTrigger(trigger)
      }
      // sc.addTrigger(new Trigger(EventTypes.OnAfterMovement, functions));
      case None => System.err.println("Applying " + this + " to tile " + tile + ", but tile is not part of any scenario.")
    }
    super.applyTo(tile)
  }
  
  override def unapplyTo(tile : Tile) : Unit = {
    attachedTile.flatMap(_.getScenarioData()) match {
      case Some(sc) => 
      case None => System.err.println("Unapplying " + this + " to tile " + tile + ", but tile is not part of any scenario.")
    }
    super.unapplyTo(tile)
  }
  
  /**
   * Render trap for allies so they know where they have been placed
   */
  import tbs.rendering._
  import javax.media.opengl.GL2
  override def render(gl : GL2) : Unit = {
    attachedTile match {
      case None =>
      case Some(tile) => {
        renderText(text = "^", x = tile.glX, y = tile.glY, width = tile.glWidth(), height = tile.glHeight(), gl = gl)
      }
    }
  }
}

