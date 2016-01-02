package tbs.game.scenario

import tbs.map.GameMap
import tbs.player._
import tbs.entity._
import tbs.action._
import tbs.effect._
import tbs.effectqueue.EffectQueue
import tbs.event._
import tbs.mouse.TBSMouseEvent
import tbs.dialog._
import tbs.rendering._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import javax.media.opengl.GL2
import java.awt.event.MouseWheelEvent
import scala.util.Random
import tbs.game.state.GameState
import tbs.tiles.Tile

/**
 * Battles.
 */
class Scenario extends GameState {
  var scenarioData : ScenarioData = new ScenarioData
  scenarioData.attachToScenario(this)
  var parseMousePressFunction : (TBSMouseEvent) => Boolean = (foo) => false
  var prvParseMousePressFunction : (TBSMouseEvent) => Boolean = (e) => false
  var parseMousePressFunctionStack : ArrayBuffer[(TBSMouseEvent) => Boolean] = new ArrayBuffer
  
  var random : Random = new Random
  var runThread : Option[Thread] = None
  
  def loadScenarioData(scInfo : ScenarioData) = {
    // Attach ScInfo to this scenario state
    scenarioData = scInfo
    scInfo.attachToScenario(this)
    scInfo.init()
  }
  
  def getScenarioData () : ScenarioData = {
    scenarioData
  }
  
  override def init() {
    scenarioData.attachToScenario(this)
    runThread = Some(new Thread(new Runnable {
    	def run() = {
    	  Scenario.this.run()
    	} 
    }))
    runThread.get.start()
  }
  
  /**
   * Turn logic
   */
  
  def getEntities() : List[Entity] = {
    scenarioData.getMap().getEntities()
  }
  
  def nextEntity(entity: Entity) : Option[Entity] = {
    var next : Option[Entity] = None
    if (scenarioData.entities.contains(entity)) {
      var nextIndex = (scenarioData.entities.indexOf(entity) + 1 ) % scenarioData.entities.size
      next = Some(scenarioData.entities( nextIndex ))
    }
    next
  }
  
  def doTurn() : Unit = {
    println("New Turn.")
    scenarioData.getCurrentEntity() match {
      case None => 
      case Some(e) => {
        while (e.canAct()) {
          performEntityTurn(e)
        }
      }
    }
    scenarioData.advanceTurn()
  }
  
  def performEntityTurn(entity : Entity) : Unit = {
    entity.player match {
      case Some(player) => player.selectAction(entity) match {
        case Some(action) => {
          if (action.isValid) {
            println("Resolving " + action + " ...")
            resolveAction(action)
          } else {
            println("No action")
          }
        }
        case _ => 
      }
      case _ => 
    }
  }

  override def parseMousePress (pressEvent: TBSMouseEvent) : Boolean = {
    scenarioData.getMap().resetDrag()
    var pressConsumed : Boolean = super.parseMousePress(pressEvent)
    if (! pressConsumed) {
      pressConsumed = parseMousePressFunction(pressEvent) 
    }
    pressConsumed
  }
  
  override def parseMouseDrag(dragEvent : TBSMouseEvent) : Boolean = {
    var dragConsumed : Boolean = dialogList.parseMouseDrag(dragEvent)
    if (! dragConsumed) {
      dragConsumed = scenarioData.getMap().parseMouseDrag(dragEvent)
    }
    dragConsumed
  }
  
  /**
   * Perform procedures to make sure the client and server are using the same Random
   * object prior to performing actions that may need RNG rolls.
   */
  def synchRandom() : Unit = {
    
  }
  
  override def parseMouseScroll(scrollEvent : MouseWheelEvent) : Boolean = {
    var scrollConsumed : Boolean = super.parseMouseScroll(scrollEvent) // Check if dialogs consumed event
    scrollConsumed = scenarioData.getMap().parseMouseScroll(scrollEvent)
    scrollConsumed
  }
 
    /**
   * Enter a state where the scenario takes input to select an entity
   * Selectable entities on the scenario map are highlighted, 
   * Dialogs are hidden, and key and mouse input are changed:
   * 
   * Clicking on a candidate entity or cycling to it with the left/right
   * arrow keys marks it as the currently selected candidate (with an arrow or 
   * some other indicator to show the player it is chosen.) 
   * 
   * Pressing enter or pressing an ok button will apply the selectionFunction()
   * 
   * Pressing escape will cancel out of Entity Selection, usually leaving the player
   * back in Action selection.
   * 
   */
  def enterEntitySelection(selectableFilter : (Entity) => Boolean, 
                           selectionFunction : (Entity) => Unit,
                           cancelFunction : () => Unit) : Unit = {
    
    var candidates = scenarioData.entities.filter(selectableFilter)
    var tileFilter : (Tile) => Boolean = (tile : Tile) => {
      var validTile : Boolean = false
      tile.getEntity() match {
        case Some(e) => validTile = selectableFilter(e)
        case _ =>
      }
      validTile
    }
    
    var tileSelectionFunc : (Tile) => Unit = (tile : Tile) => {
      tile.getEntity() match {
        case Some(e) => selectionFunction(e)
        case _ => assert(false) // SHOULD NOT HAPPEN.
      }
    }
    enterTileSelection(tileFilter, tileSelectionFunc, cancelFunction)
  }
    
  def exitEntitySelection() : Unit = {
    // Stop highlighting anything, set input to normal
    for (entity <- scenarioData.entities) yield {
      // entity.unhighlight()
    }
    // Clicking on 
    parseMousePressFunction = (mEvent) => {
      false
    }
  }
  
  /**
   * Enter 'tile selection' mode. This allows local players to select 
   * a tile, which then fires the selectionFunction. AI and Network
   * players should not use this function as they do not need a GUI
   * input method for selecting tiles.
   * 
   * @param selectableFilter Filter function to indicate which tiles on the
   * map are selectable by the player.
   * 
   * @param selectionFunction Function to be applied to the tile selected
   * 
   * @param cancelFunction    Function to be applied when selection is cancelled
   */
  def enterTileSelection(selectableFilter : (Tile) => Boolean, 
                           selectionFunction : (Tile) => Unit,
                           cancelFunction : () => Unit) : Unit = {
    println("Entering tile selection")
    prvParseMousePressFunction = parseMousePressFunction
    
    parseMousePressFunction = (e) => {
      var pressConsumed : Boolean = false
      if (e.isRightMouseButton()) {
        cancelFunction()
        exitTileSelection()
        pressConsumed == true
      } else {
        scenarioData.getMap().getTiles().filter(selectableFilter).foreach(
          (t) => {
            if (t.isGLCoordWithinTile(e.mX, e.mY)) {
              selectionFunction(t)
              exitTileSelection()
              pressConsumed = true
            }
          }
        )
      }
      pressConsumed
    }
    
    for (tile <- scenarioData.getMap().getTiles().filter(selectableFilter)) {
      tile.setHighlighted(true)
    }
  }
  
  def exitTileSelection() : Unit = {
    for (tile <- scenarioData.getMap().getTiles()) {
      tile.setHighlighted(false)
    }
    // Restore previous mouse pressing logic
    parseMousePressFunction = prvParseMousePressFunction 
  }
  
  def enqueueAction(action : Action) : Unit = {
    
  }
  
  def resolveAction(action : Action) : Unit = {
    action.resolve()
  }
  
  def enqueueEffect(effect: Effect) : Unit = {
    scenarioData.enqueueEffect(effect)
  }

  def effectQueue : EffectQueue = {
    scenarioData.effectQueue
  }
  
  def resolveEffects() : Unit = {
    scenarioData.resolveEffects()
  }
  
  /**
   * Rendering
   */
  override def render(gl: GL2) = {

    scenarioData.render(gl)
    dialogList.render(gl)
    
  }
  
  override def run() = {
    while (true) {
      // doTurn()
    }
  }

}