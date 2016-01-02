package tbs.action

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer

import tbs.effect._
import tbs.entity._
import tbs.map._
import tbs.tiles.Tile
import tbs.id.HasID
import tbs.game.scenario._
import tbs.targeting._
import tbs.tiles.TacticalTile

// Actions are what are used by actors. Everything else that happens that is not
// a conscious actor choice is an effect.
class Action extends Serializable with HasID {
  var executeF : () => Unit = () => {}
  // We want to pass around strings during networking and other syncing, not the entire user field
  @volatile var userID : Option[String] = None
  @transient private var user : Option[Entity] = None
  protected var apCost : Int = 0
  
  def setUser(newUser : Entity) {
    if (newUser != null) {
      user = Some(newUser)
      userID = Some(newUser.getID())
    }
    else {
      user = None // Use None for inappropriate entities (i.e. null values)
      userID = None
    }
  }
  
  def getUserID() : Option[String] = {
    userID
  }
  
  def getUser() : Option[Entity] = {
    user
  }
  
  def getScenarioData() : Option[ScenarioData] = {
    getUser().flatMap(_.getScenarioData())
  }
  
  /**
   * Check whether the entity can actually use this action.
   * This includes checking if:
   * - The actor enough AP/MP or resources
   * - The action not being on cooldown or disabled
   * - The actor being in a state to use actions (not sleeping, dead, etc.)
   * - The action having at least one possible target.
   */
  def isUseable() : Boolean = {
    user != None
  }
  
  /**
   * Checks to see if an action can be executed with the current parameters, or if 
   * it contains invalid targets etc. For example, an isValid check for a movement action
   * will factor in if the target tile is too far to be reachable.
   */
  def isValid() : Boolean = {
    false
  }
  
  /**
   * Basically a wrapper around the execute method with the necessary paperwork being done before
   * and after the execution.
   */
  final def resolve() = {
    execute()
  }
  
  protected def execute() = {
  }
  
  def animate : Unit = {
  }
  
  override def toString() = {
    Action.this.getClass().getSimpleName() + "," +
    user.toString() + " at " + getUser().flatMap(_.getCurrentTile())
  }
  
  /**
   * Serialization related stuff
   */

  def syncToScenarioData(scData : ScenarioData) {
  	getUserID() match {
  	  case Some(id) => {
  	    scData.getEntityByID(id) match {
  	      case Some(entity) => setUser(entity)
  	      case _ => assert(false)
  	    }
  	  }
  	  case _ => assert(false)
  	}
  }
}

/**
 * 
 */
trait Proactive

/**
 * Involuntary actions, such as counter-attacking enemies after they hit you.
 */
trait Reactive



class EntityTargetingAction extends Action with Targeting[Entity] {
  import tbs.game.scenario.Scenario;
  
  override def getValidTargets() : List[Entity] = {
    getUser().flatMap(_.getScenarioData()) match {
      case Some(scData) => scData.getEntities().filter((entity : Entity) => isValidTarget(entity))
      case _ => Nil
    }
  }
  
  override def isUseable() : Boolean = {
    super.isUseable() && 
    ! getValidTargets().isEmpty
  }
  
  override def isValid() : Boolean = {
    (getUser(), getTarget()) match {
      case (Some(user), Some(tar)) if isValidTarget(tar) => true
      case _ => false
    }
  }
  
  override def syncToScenarioData(scData : ScenarioData) = {
    super.syncToScenarioData(scData)
    this.getTargetID() match {
      case Some(id) => {
        scData.getEntityByID(id) match {
          case Some(entity) => setTarget(entity)
          case _ => assert(false)
        }
      }
      case _ => 
    }
  }
}

class TileTargetingAction extends Action with Targeting[TacticalTile] {
  import tbs.game.scenario.Scenario
  
  override def isUseable() : Boolean = {
    ! getValidTargets().isEmpty
  }
  
  override def getValidTargets() : List[TacticalTile] = {
    getUser().get.currentMap match {
      case Some(map : TacticalMap) => map.tacticalTiles.filter(isValidTarget).toList
      case _ => Nil
    }
  }
  
  override def isValid() : Boolean = {
    var isValid : Boolean = false
    (getUser(), getTarget()) match {
      case (Some(user), Some(tar)) if isValidTarget(tar) => isValid = true
      case _ => isValid = false
    }
    isValid
  }
  
  override def syncToScenarioData(scData : ScenarioData) = {
    super.syncToScenarioData(scData)
    this.getTargetID() match {
      case Some(id) => {
        scData.getTileByID(id) match {
          case Some(tile : TacticalTile) => 
            setTarget(tile)
          case _ => assert(false)
        }
      }
      case None => 
    }
  }
}

