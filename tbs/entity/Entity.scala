package tbs.entity

import scala.collection.mutable._
import javax.media.opengl.GL2
import scala.math._
import java.util.UUID

import tbs.tiles.Tile
import tbs.rendering._
import tbs.mouse._
import tbs.factions.Faction
import tbs.effect._
import tbs.action._
import tbs.map._
import tbs.movement._
import tbs.damage._
import tbs.game.scenario._
import tbs.event._
import tbs.player._
import tbs.id.HasID

trait SelectableEntity {
  var isSelected = false;
  
  def select() = {
    isSelected = true;
    // Highlight
  }
}

class Entity extends Renderable with HasID with EffectAttachable[Entity] with PartOfScenario {
  var commonNoun : String = "Entity"
  var properNoun : String = "Nameless"
  var glyph : Char = '?'
  var glyphColor : (Double, Double, Double) = (1d, 1d, 1d)
  
  // Can other entities pass through this square?
  var isPassable : Boolean = false
  var isTargetable : Boolean = false
  
    
  var currentMap : Option[GameMap] = None
  var currentScenarioData : Option[ScenarioData] = None
  var currentTile : Option[Tile] = None
 
  val actions : ArrayBuffer[Action]  = new ArrayBuffer(0)
  
  /**
   * Attribute access methods.
   */
  
  val attributes : EntityAttributeBlock = new EntityAttributeBlock
  
  def getAttributes () : EntityAttributeBlock = {
    attributes
  }
  
  def getAttribute (id : String) : Option[EntityAttribute] = {
    attributes.getAttribute(id)
  }
  
  def addAttribute(id : String, attr : EntityAttribute) = {
    attributes.addAttribute(id, attr)
  }
  
  def removeAttribute(id : String) = {
    attributes.removeAttribute(id)
  }

  
  /**
   * The learn set that an entity may learn over their progression over a game. 
   * The learn set is a list of 2-tuples, the first field being the level at which the 
   * action is learned, and the 2nd field being the Action itself.
   * 
   * For example, when an entity is level 10, the actions it can contain all of
   * actionLearnSet.filter(_._1 < 10)
   */
  protected var actionSetByLevel : ListBuffer[(Int, Action)] = new ListBuffer[(Int, Action)]()
  
  def addActionToLearnSet (action : (Int, Action)) : Unit = {
    actionSetByLevel.append(action)
  }
  
  def removeActionFromLearnSet (action : Action) : Unit = {
    actionSetByLevel = actionSetByLevel.filterNot(_._2 == action)
  }
  
  /**
   * @return
   * An array of all actions useable at the current level.
   */
  def getActionSetOnLevel(level : Int) : Array[Action] = {
    val actions = new ListBuffer[Action]
    for (pair <- actionSetByLevel.filter(_._1 < level)) {
      actions.append(pair._2)
    }
    actions.toArray[Action]
  }
  
  /**
   * Who is controlling this actor? There are 4 options -
   * LocalPlayer, RemotePlayer, AI and None.
   * AI is only visible on the server, on clients the AI's command will come in
   * via the RemotePlayer stream.
   * None is no player assigned. May be used if a player drops out.
   */
  var _player : Option[Player] = None
  
  override def hashCode() : Int = {
    getID().hashCode()
  }
  
  /**
   * Returns the x and y difference between this and another entity. Functionally 
   * equivalent to the absolute difference between x and y coordinates of the two entities, except that
   * -1 is returned if entities are not placed on the same map.
   * 
   * Calls the tile.xyDistanceTo(other tile) method. 
   * 
   * @return The sum of the x and y difference of the tile coordinates of the entities if they are placed
   *         on the same map.
   *         -1 if entities are not placed on tiles, or not on the same map.
   *         
   */
  def xyMapDistanceToEntity(other : Entity) : Int = {
    var distance : Int = -1
    ( this.getCurrentTile() , other.getCurrentTile() ) match {
      case ( Some(t1), Some(t2) ) => distance = t1.xyMapDistanceTo(t2)
      case _ => 
    }
    distance
  }
  
  /**
   * Wrapper around the tile.xyDistanceTo(other tile) method
   */
  def xyMapDistanceToTile(tile : Tile) : Int = {
    var distance : Int = -1
    this.getCurrentTile() match {
      case Some(currTile) => distance = currTile.xyMapDistanceTo(tile)
      case _ =>
    }
    distance
  }
  
  /**
   * Checks if this entity's controller is an enemy of the opponent's controller.
   */
  def isEnemyOf(other : Entity) : Boolean = {
    ( this.player, other.player ) match {
      case (Some(pl1), Some(pl2)) => false // @TODO : pl1.getFaction() isEnemyOf pl2.getFaction()
      case _ => false // Non controlled entities are not enemies of anyone
    }
  }
  
  def isAllyOf(other : Entity) : Boolean = {
    ( this.player, other.player ) match {
      case (Some(pl1), Some(pl2)) => false 
      case _ => false // Non controlled entities are not allies of anyone
    }
  }
  
  /**
   * Adds an action to the list the creature can use on their turn. Action
   * must be an active ability, with the ActorSourced trait. Doubles of the
   * same ability instance are added only once.
   * 
   * @param ab the action to give this actor.
   * 
   * @
   * The number of active actions this actor has available for use.
   */
  def giveAction(ab : Action) : Int = {
    ab.setUser(this)
    if (! hasAction(ab)) {
    	actions += ab;
    }
    actions.size
  }
  
  /**
   * The list of actions this actor possesses
   */
  def getActions() : Array[Action] = {
    actions.toArray
  }
  
  /**
   * Returns the subset of actions which the entity can actually use at the current time.
   * For an action to be useable, it must have enough resources (MP, AP, HP) to use it, and there
   * must be at least one target it can be used on.
   */
  def getUseableActions() : Array[Action] = getActions.filter((action : Action) => action.isUseable)
  
  /**
   * Entity has the same class of action as the param passed in. 
   */
  def hasAction(a : Action) : Boolean = {
    ! getActions.filter((cand) => cand.getClass() == a.getClass()).isEmpty
  }
  
  /**
   * The meat of action choosing and execution occurs here. 
   */
    /**
   * The current controller has a go. 
   */
  def selectAction(scenario: Scenario) : Option[Action] = {
    var selectedAction : Option[Action] = None
    _player match {
      case None => // End turn, we have no-one to control this actor
      case Some(pl) => selectedAction = pl.selectAction(this)
    }
    selectedAction
  }
  
  /**
   * @return
   * True  iff the Entity has 1 or more Actions/Movements it can use.
   * False iff the Entity no longer can do anything. 
   * 
   * @note
   * False -> that the entity's turn should end, if it is currently their turn. 
   */
  def canAct() : Boolean = {
    _player != None &&
    ! getActions().filter((action: Action) => action.isUseable()).isEmpty
  }
  
  /**
   * @note
   * Do not call this to place an entity. Call placeAtTile, which invokes this
   * along with some other methods.
   */
  def setCurrentTile(t : Tile) : Unit = {
    currentTile = Some(t)
    currentMap = t.currentMap
  }
  
  /**
   * Call to put the entity onto a tile. 
   * Tile does not need to be part of a map or scenario.
   * 
   * @postcondition
   * If Tile is part of scenario, this Entity is also added to the scenario.
   */
  def placeAtTile(t : Tile) : Unit = {
    t.placeEntity(this)
  }
  
  /**
   * Return the current tile this entity is sitting on. 
   * 
   * @return
   * None if entity is not on a tile (not placed in a battle).
   * Some(Tile) if this entity is placed.
   * 
   * @invariable
   * The tile returned by this function should also have this entity attached.
   * If this tile returns None, no tile should have this entity attached. 
   */
  def getCurrentTile() : Option[Tile] = {
    currentTile
  }
  
  /**
   * Deal damage to a creature attribute. For example, an attack that hurts 
   * our entity will deal some damage to the CURRENTHP attribute. 
   * 
   * Please do not use this method to heal an entity with a negative damage value,
   * as it will call events that fire on actual damage. (Such as triggering damage resistance,
   * retaliation, etc.)
   * 
   * @precondition
   * The current entity must be actually attached to scenario data before 
   * it takes damage. Change this method if you think there is a case where the entity 
   * will be taking damage outside of a battle.
   */
  def applyDamage(dmg : DamageData) = {
    // game.notify (new beforeDamageEvent(dmg))
    getScenarioData().get.onBeforeDamage.fireFunctions(dmg)
    dmg.getTargetAttribute(this).subtractFromValue(dmg.value)
    getScenarioData().get.onAfterDamage.fireFunctions(dmg)
  }
  
  /**
   * Heal (recover) an attribute. Do not use this method to boost or buff stats beyond
   * their normal levels, only to 'regain' lost hitpoints and stat attributes. Does not 
   * check if an entity has 'died' after healing, so don't be cute and try and heal 
   * negative hit points to do damage.
   * 
   * @precondition
   * The entity must be attached to some scenarioData (as part of a battle). 
   * Do not try to heal an entity on the world map with this method.
   */
  def heal(healData : tbs.heal.HealData) = {
    getScenarioData().get.onBeforeHeal.fireFunctions( healData )
    attributes.currentHP.addToValue(healData.healingAmount)
    getScenarioData().get.onAfterHeal.fireFunctions( healData )
  }
  
  /**
   * Return the map this entity is currently part of. 
   * 
   * @return
   * Some(GameMap) if this creature is currently on a map, 
   * None if the creature has not been placed on a map tile.
   * 
   * If this creature is attached to a tile, this value
   * should be equivalent to getCurrentTile().getCurrentMap()
   */
  def getCurrentMap() : Option[GameMap] = {
    currentMap
  }
  
  /**
   * Return the scenario data of the battle this entity is in.
   * 
   * @return
   * None if this entity is not attached to scenario data (Not in a battle)
   * Some(ScenarioData) otherwise
   */
  def getScenarioData() : Option[ScenarioData] = {
    currentScenarioData
  }
  
  /**
   * Simple wrapper to save on calling 'currentScenario.flatMap(._getScenario)'
   * 
   * @return
   * Some(Scenario) if entity is current in a scenario, 
   * None           otherwise
   */
  def scenario(): Option[Scenario] = {
    currentScenarioData.flatMap(_.getScenario())
  }
  
  def setCurrentScenario(sc : Option[ScenarioData]) : Unit = {
    currentScenarioData = sc
    _player match {
      case Some(c) => c.currentScenarioData = sc
      case None => 
    }
  }
  
  /**
   * Return the player in charge of controlling this entity
   */
  def player : Option[Player] = {
    _player
  }
  
  def player_=(player : Option[Player]): Unit = {
    player match {
      case Some(p) => p.currentScenarioData = currentScenarioData
      case _ =>
    }
    this._player = player
  }
  
  def player_=(player : Player): Unit = {
    player_=(Some(player))
  }
  
  /**
   * Called to let the entity know its turn is up. Non-Actor Entities can't actually act, so they'll do
   * the same thing (usually nothing) and then end their turn.
   * 
   * Actors can act, and will usually call their players to do something on their turn.
   */
  def startTurn(battleState: ScenarioData) = {
    // Recover MP, AP
    // Fire Turn Start Events
    /**
     * @todo FIRE A TURN START EVENT
     */
  }
  
  def notifyScenario(eventType : String, info : Object) : Unit = {
    
  }
  
  
  /**
   * RENDERING STUFF
   */
  
  /**
   * Binds our entity width / height to the tile's width and height.
   */
  override def glX() : Double = {
    var x = 0d
    currentTile match {
      case Some(t) => x = t.glX
      case _ =>
    }
    x
  }
  
  override def glY() : Double = {
    var y = 0d
    currentTile match {
      case Some(t) => y = t.glY
      case None =>
    }
    y
  }
  
  override def glWidth() : Double = {
    var w = 0d
    currentTile match {
      case Some(t) => w = t.glWidth
      case None => 
    }
    w
  }
  
  override def glHeight() : Double = {
    var h = 0d
    currentTile match {
      case Some(t) => h = t.glHeight
      case None => 
    }
    h
  }
    
  val color = (1, 1, 1)
  
  override def render(gl : GL2) : Unit = {
    // renderText(Character.toString(glyph), glX(), glY(), glWidth() , glHeight() * 2, gl)
  }
  
  override def renderAtRect(gl : GL2, bottomLeft : (Int, Int), topRight : (Int, Int)) = {
    renderGlyph(gl, glyph, glyphColor, bottomLeft, topRight)
  }
  
  override def update() : Unit = {
    
  }
}

  /**
   * PRIMARY ATTRIBUTES
   * ---------------------------------------------------------
   * ---------------------------------------------------------
   */

/**
 * Entity stat attribute. 
 * 
 */
class EntityAttribute (startingValue : Double = 1d) extends Serializable {
  protected var value : Double = startingValue
  
  def setValue (d : Double) : Unit = {
    value = d
  }
  
  def addToValue (d : Double) : Unit = {
    setValue(getValue() + d)
  }
  
  def subtractFromValue (d : Double) : Unit = {
    addToValue(-d)
  }
  
  final def getValue () : Double = value
}

/**
 * Entity stat array. Each entity has common attributes which are defined below
 * (CurrentHP, etc.)
 * 
 * If needed, custom attributes can be added to the attributeMap. For example, 
 * if a sanity attribute needed to be added to an entity (for some reason), just
 * call entity.attributes.add("CurrentSanity", SanityAttribute) , where SanityAttribute
 * is your own sanity class.
 * 
 * The string identifiers are *not* case sensitive
 */
class EntityAttributeBlock extends Serializable {
  // We store the attributes in a map so we can easily index and add attributes
  // on the fly
  val attributeMap = new HashMap[String, EntityAttribute];
  
  /**
   * Add a new attribute to the block map.
   * 
   * @param s Identifier String. Not case sensitive.
   * @param a The EntityAttribute object to add
   */
  def addAttribute(s : String, a: EntityAttribute) : Unit = {
    if (attributeMap.contains(s.toLowerCase())) 
      System.err.println("Attribute " + s + " is being overwritten.")
    attributeMap(s.toLowerCase) = a
  }
  
  /**
   * Remove an attribute by string key from the attribute map.
   */
  def removeAttribute(s : String) : Unit = {
    if (attributeMap.contains(s.toLowerCase))
      attributeMap.remove(s.toLowerCase)
  }
  
  /** 
   *  Ignores case. Return mapped creature attribute (if any) whose
   *  string ID matches the argument passed. 
   *  
   *  @return
   *  Some(EntityAttribute) if one is found, 
   *  None otherwise
   */
  def getAttribute(s : String) : Option[EntityAttribute] = {
    if (attributeMap.contains(s.toLowerCase)) {
      Some(attributeMap(s.toLowerCase))
    } else {
      None
    }
  }
  
  var currentHP : EntityAttribute = _
  var maximumHP : EntityAttribute = _
  
  /**
   * MP is magic points, staple of RPGs everywhere. Many actions have an MP cost.
   * 
   * Some MP is regenerated at the beginning of each turn.
   */
  var currentMP : EntityAttribute = _
  var maximumMP : EntityAttribute = _
  
  /**
   *   AP, or action points. 
   *   
   *   The vast majority of active actions a creature can use have an associated
   *   AP cost. AP is replenished at the beginning of each turn. There should
   *   be few ways of increasing or deducting AP, and most if not all should affect
   *   maxAP rather than currentAP.
   */
 
  var currentAP : EntityAttribute = _
  var maximumAP : EntityAttribute = _
  
  currentHP = new EntityAttribute {
    override def setValue(d : Double) = {
      value = min( max(d, 1), maximumHP.getValue() )
    }
  }
  
  maximumHP = new EntityAttribute {
    override def setValue(d : Double) = {
      value = max(d, 1)
      if (currentHP.getValue() > this.getValue())
        currentHP.setValue(this.getValue())
    }
  }
  
  maximumMP = new EntityAttribute {
    override def setValue(d : Double) = {
      value = max(d, 0)
      if (currentMP.getValue() > this.getValue())
        currentMP.setValue(this.getValue())
    }  
  }
  
  currentMP = new EntityAttribute {
    override def setValue(d : Double) = {
      value = min( max(d, 0), maximumMP.getValue())
    }
  }
  
  
  currentAP = new EntityAttribute {
    override def setValue(d : Double) = {
      value = min (max(d, 0), maximumAP.getValue())
    }
  }
  
  maximumAP = new EntityAttribute {
    override def setValue(d : Double) = {
      value = max(d, 0)
      if (currentAP.getValue() > this.getValue()) 
        currentAP.setValue(this.getValue())
    }
  }
  
  val currentMagicPower = new EntityAttribute {
    override def setValue(d : Double) = {
      value = max(d, 0)
    }
  }
  
}

