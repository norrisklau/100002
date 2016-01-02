package tbs.game.scenario

import tbs.map._
import tbs.player._
import tbs.entity._
import tbs.action._
import tbs.effect._
import tbs.event._
import tbs.mouse.TBSMouseEvent
import tbs.dialog._
import tbs.rendering._
import tbs.game.state.GameState
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import javax.media.opengl.GL2
import java.awt.event.MouseWheelEvent
import scala.util.Random
import tbs.effectqueue.EffectQueue
import tbs.random.UsesRandom
import tbs.tiles.Tile


class ScenarioData extends Renderable with Serializable
                                    with TBSEventListener
                                    with UsesRandom {
  private var map : GameMap = tbs.map.TowerMapGenerator.generateMaze(12, 12)
  private var random : Random = new Random()
  map.setScenarioData(this)
  var players : ListBuffer[Player] = new ListBuffer[Player]
  var entities : ArrayBuffer[Entity] = new ArrayBuffer[Entity]
  var currentEntity : Option[Entity] = None
  private var entityTurnList : ArrayBuffer[Entity] = new ArrayBuffer[Entity]
  
  @transient private var attachedScenario : Option[Scenario] = None
  val actionQueue : ArrayBuffer[Action] = new ArrayBuffer[Action] ()
  @transient val effectQueue : EffectQueue = new EffectQueue()
  
  def getMap() : GameMap = {
    map
  }
  
  /**
   * Adding an entity to a scenario does not mean it will appear on the turn
   * list, but it will listen to events on the map and react.
   */
  def addEntity(e : Entity) = {
    entities.append(e)
  }
  
  /**
   * Remove an entity, taking it off the map and turn list if necessary.
   */
  def removeEntity(e : Entity) = {
    map.removeEntities((a) => a == e)
    removeEntityFromTurnList(e)
  }
  
  /**
   * Entity TURN related methods
   */

  /**
   * Add an entity to the turn list, that is, entities that will get a turn to act
   * as the game progresses.
   */
  def appendEntityToTurnList (e : Entity) = {
    insertEntityInTurnList(e, entityTurnList.size)
  }

  private def insertEntityInTurnList (e : Entity, index : Int) = {
    removeEntityFromTurnList(e)
    entityTurnList.insert(index, e)
  }
  
  /**
   * Add an entity to the turn list, placing it just before another entity in the ordering.  
   */
  def insertEntityInTurnListBefore(entityToInsert : Entity, referenceEntity : Entity) = {
    if (entityTurnList.contains(referenceEntity)) {
      insertEntityInTurnList(entityToInsert, entityTurnList.indexOf(referenceEntity))
    }
  }
  
  /**
   * Add an entity to the turn list, placing it just before after entity in the ordering.  
   */
  def insertEntityInTurnListAfter(entityToInsert : Entity, referenceEntity : Entity) = {
    if (entityTurnList.contains(referenceEntity)) {
      insertEntityInTurnList(entityToInsert, entityTurnList.indexOf(referenceEntity) + 1)
    }
  }
  
  /**
   * Remove an entity from the turn list, such that it never gets a turn. If multiple 
   * copies of the same entity are in the turn list, remove all of them.
   * 
   * @return
   * True if one or more of the entity were removed from the turn list
   * False if the entity was not in the turn list to start with
   */
  def removeEntityFromTurnList (entityToRemove : Entity) : Boolean = {
    val entityExists : Boolean = entityTurnList.contains(entityToRemove)
    entityTurnList = entityTurnList.filterNot((e) => {e == entityToRemove})
    entityExists
  }
  
  /**
   * Return the entity whose turn it is at the moment.
   * 
   * @return
   * Some(Entity) if there is at least one entity in the turn list, where that entity
   * would be the current one in the turn order.
   * None iff there are no entities that will currently be having a turn. 
   */
  def getCurrentEntity() : Option[Entity] = {
    currentEntity
  }
  
  def setCurrentEntity(e : Entity) = {
    currentEntity = Some(e)
  }
  
  def startTurn(entity : Entity) = {
    onBeforeTurnStart.fireFunctions(entity)
  }
  
  def endTurn(entity : Entity) = {
    
  }
  
  /**
   * Give the next entity on the turn list a turn.
   */
  def advanceTurn () = {
    getCurrentEntity() match {
      case None => if (! entityTurnList.isEmpty) setCurrentEntity(entityTurnList(0))
      case Some(e) => {
        val nextEntity = entityTurnList( (entityTurnList.indexOf(e) + 1) % entityTurnList.size)
        setCurrentEntity(nextEntity)
      }
    }
  }
  
  /**
   * Start up the scenario, connecting all the 'pieces' (entities, tiles) to each other
   * and to this overall scenario. 
   * 
   * For example, entities will have their current scenario fields set to this.
   */
  def init() = {
    map.setScenarioData(this)
    for(e <- getEntities()) {
      e.setCurrentScenario(Some(this))
      for (a <- e.getActions()) {
        a.syncToScenarioData(this)
      }
    }
  }
  
  def getScenario() : Option[Scenario] = {
    attachedScenario
  }
  
  def getEntities() : List[Entity] = {
    entities.toList
  }
  
  def enqueueEffect(effect : Effect) : Unit = {
    effectQueue.enqueue(effect)
  }
  
  def resolveEffects() : Unit = {
    effectQueue.resolve(this)
  }
  
  /**
   * Look for an entity that has the same string id in the scenario.
   */
  def getEntityByID(id : String) : Option[Entity] = {
    var matchingEntity : Option[Entity] = None
    for (e <- getEntities()) {
      if (e.getID().matches(id))
        matchingEntity = Some(e)
    }
    matchingEntity
  }
  
  def getTileByID(id : String) : Option[Tile] = {
    map.getTileByID(id)
  }
  
  def getPlayers() : Array[Player] = {
    players.toArray[Player]
  }
  
  def attachToScenario(sc : Scenario) : Unit = {
    attachedScenario = Some(sc)
    for (p <- players) yield {
      p.currentScenarioData = Some(this)
    }
    for (e <- entities) yield {
      e.setCurrentScenario(Some(this))
    }
  }
  
  override def render(gl: GL2) = {
    map.render(gl)
    for (entity <- entities) yield {
      entity.render(gl)
    }
  }
  

  override def toString() : String = {
    super.toString()
    /**
    var str : String = "Scenario " + super.toString() + " containing ENTITIES: \n"
    for (e <- getEntities()) {
      str += e.toString() + " containing ACTIONS: \n"
      for (a <- e.getActions) {
        str += a.toString() + "\n"
      }
    }
    for (p <- getPlayers()) {
      str += p.toString() + "\n"
    }
    str
    **/
  }
}
